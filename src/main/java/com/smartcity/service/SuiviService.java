package com.smartcity.service;

import com.smartcity.utils.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de suivi politique : historique statuts, évaluations, KPIs.
 */
public class SuiviService {

    private static final Logger logger = LoggerFactory.getLogger(SuiviService.class);
    private static volatile boolean schemaReady = false;

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private void ensureSchema() {
        if (schemaReady) return;
        synchronized (SuiviService.class) {
            if (schemaReady) return;
            try (Connection conn = getConn();
                 Statement st = conn.createStatement()) {
                st.execute("""
                    CREATE TABLE IF NOT EXISTS HistoriqueStatut (
                        idHistorique INT AUTO_INCREMENT PRIMARY KEY,
                        idSignalement INT NOT NULL,
                        ancienStatut VARCHAR(50),
                        nouveauStatut VARCHAR(50) NOT NULL,
                        idAuteur INT NULL,
                        dateChangement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        commentaire TEXT NULL,
                        FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
                        FOREIGN KEY (idAuteur) REFERENCES Utilisateur(idUser) ON DELETE SET NULL,
                        INDEX idx_hist_sig (idSignalement),
                        INDEX idx_hist_date (dateChangement)
                    )
                    """);
                st.execute("""
                    CREATE TABLE IF NOT EXISTS EvaluationCollecte (
                        idEvaluation INT AUTO_INCREMENT PRIMARY KEY,
                        idSignalement INT NOT NULL UNIQUE,
                        idCitoyen INT NOT NULL,
                        note TINYINT NOT NULL CHECK (note BETWEEN 1 AND 5),
                        commentaire TEXT NULL,
                        dateEvaluation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (idSignalement) REFERENCES Signalement(idSignalement) ON DELETE CASCADE,
                        FOREIGN KEY (idCitoyen) REFERENCES Utilisateur(idUser) ON DELETE CASCADE
                    )
                    """);
                // Créer les vues seulement si elles n'existent pas encore
                if (!viewExists(conn, "vue_suivi_signalement")) {
                    st.execute("""
                        CREATE VIEW vue_suivi_signalement AS
                        SELECT
                            s.idSignalement,
                            s.description,
                            s.categorie,
                            s.statut,
                            s.dateSignalement,
                            z.nomZone,
                            u_citoyen.nom AS nomCitoyen,
                            u_citoyen.email AS emailCitoyen,
                            u_agent.nom AS nomAgent,
                            a.dateAffectation,
                            a.dateCollecte,
                            TIMESTAMPDIFF(HOUR, s.dateSignalement, IFNULL(a.dateCollecte, NOW())) AS heuresEcoules,
                            CASE
                                WHEN s.statut = 'Termine' AND a.dateCollecte IS NOT NULL
                                    THEN TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateCollecte)
                                ELSE NULL
                            END AS heuresResolution,
                            e.note AS noteEvaluation,
                            e.commentaire AS commentaireEvaluation
                        FROM Signalement s
                        LEFT JOIN Zone z ON s.idZone = z.idZone
                        LEFT JOIN Utilisateur u_citoyen ON s.idUser = u_citoyen.idUser
                        LEFT JOIN Affectation a ON s.idSignalement = a.idSignalement
                        LEFT JOIN Utilisateur u_agent ON a.idAgent = u_agent.idUser
                        LEFT JOIN EvaluationCollecte e ON s.idSignalement = e.idSignalement
                        """);
                }
                if (!viewExists(conn, "vue_kpi_zone")) {
                    st.execute("""
                        CREATE VIEW vue_kpi_zone AS
                        SELECT
                            z.nomZone,
                            COUNT(s.idSignalement) AS total,
                            SUM(s.statut = 'En attente') AS enAttente,
                            SUM(s.statut IN ('En cours','Affecte')) AS enCours,
                            SUM(s.statut = 'Termine') AS termines,
                            ROUND(SUM(s.statut = 'Termine') * 100.0 / NULLIF(COUNT(*), 0), 1) AS tauxResolution,
                            ROUND(AVG(CASE WHEN s.statut = 'Termine' AND a.dateCollecte IS NOT NULL
                                      THEN TIMESTAMPDIFF(HOUR, s.dateSignalement, a.dateCollecte) END), 1) AS tempsResolutionMoyenH,
                            SUM(s.statut = 'En attente' AND s.dateSignalement < DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS urgents
                        FROM Signalement s
                        LEFT JOIN Zone z ON s.idZone = z.idZone
                        LEFT JOIN Affectation a ON s.idSignalement = a.idSignalement
                        GROUP BY z.idZone, z.nomZone
                        """);
                }
                schemaReady = true;
            } catch (SQLException e) {
                logger.warn("Schéma Suivi incomplet: {}", e.getMessage());
                // Ne pas marquer schemaReady=true : on retentera au prochain appel
            }
        }
    }

    private boolean viewExists(Connection conn, String viewName) {
        String sql = "SELECT COUNT(*) FROM information_schema.VIEWS "
            + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, viewName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // ============================================================
    // HISTORIQUE DES STATUTS
    // ============================================================

    public List<HistoriqueEntry> getHistoriqueBySignalement(int idSignalement) {
        ensureSchema();
        List<HistoriqueEntry> list = new ArrayList<>();
        String query = "SELECT h.*, u.nom AS nomAuteur FROM HistoriqueStatut h "
            + "LEFT JOIN Utilisateur u ON h.idAuteur = u.idUser "
            + "WHERE h.idSignalement = ? ORDER BY h.dateChangement ASC";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSignalement);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistoriqueEntry e = new HistoriqueEntry();
                    e.idHistorique   = rs.getInt("idHistorique");
                    e.idSignalement  = idSignalement;
                    e.ancienStatut   = rs.getString("ancienStatut");
                    e.nouveauStatut  = rs.getString("nouveauStatut");
                    e.nomAuteur      = rs.getString("nomAuteur");
                    Timestamp ts = rs.getTimestamp("dateChangement");
                    if (ts != null) e.dateChangement = ts.toLocalDateTime();
                    e.commentaire    = rs.getString("commentaire");
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur getHistoriqueBySignalement #{}", idSignalement, ex);
        }
        return list;
    }

    /** Enregistre manuellement un changement (pour les cas hors trigger). */
    public void enregistrerChangement(int idSignalement, String ancienStatut,
                                      String nouveauStatut, Integer idAuteur, String commentaire) {
        ensureSchema();
        String query = "INSERT INTO HistoriqueStatut "
            + "(idSignalement, ancienStatut, nouveauStatut, idAuteur, commentaire) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSignalement);
            ps.setString(2, ancienStatut);
            ps.setString(3, nouveauStatut);
            if (idAuteur != null) ps.setInt(4, idAuteur); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, commentaire);
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Erreur enregistrerChangement", ex);
        }
    }

    // ============================================================
    // ÉVALUATIONS CITOYENS
    // ============================================================

    public boolean ajouterEvaluation(int idSignalement, int idCitoyen, int note, String commentaire) {
        ensureSchema();
        if (note < 1 || note > 5) return false;
        String query = "INSERT INTO EvaluationCollecte (idSignalement, idCitoyen, note, commentaire) "
            + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE note=VALUES(note), commentaire=VALUES(commentaire)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSignalement);
            ps.setInt(2, idCitoyen);
            ps.setInt(3, note);
            ps.setString(4, commentaire);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.error("Erreur ajouterEvaluation", ex);
            return false;
        }
    }

    public EvaluationEntry getEvaluationBySignalement(int idSignalement) {
        ensureSchema();
        String query = "SELECT * FROM EvaluationCollecte WHERE idSignalement = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSignalement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EvaluationEntry e = new EvaluationEntry();
                    e.idSignalement = idSignalement;
                    e.note          = rs.getInt("note");
                    e.commentaire   = rs.getString("commentaire");
                    Timestamp ts = rs.getTimestamp("dateEvaluation");
                    if (ts != null) e.dateEvaluation = ts.toLocalDateTime();
                    return e;
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur getEvaluationBySignalement", ex);
        }
        return null;
    }

    public double getNoteMoyenneGlobale() {
        ensureSchema();
        String query = "SELECT AVG(note) FROM EvaluationCollecte";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException ex) {
            logger.error("Erreur getNoteMoyenneGlobale", ex);
            return 0.0;
        }
    }

    public double getNoteMoyenneParAgent(int idAgent) {
        ensureSchema();
        String query = "SELECT AVG(e.note) FROM EvaluationCollecte e "
            + "JOIN Affectation a ON a.idSignalement = e.idSignalement "
            + "WHERE a.idAgent = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idAgent);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException ex) {
            logger.error("Erreur getNoteMoyenneParAgent #{}", idAgent, ex);
            return 0.0;
        }
    }

    public double getNoteMoyenneParZone(String nomZone) {
        ensureSchema();
        String query = "SELECT AVG(e.note) FROM EvaluationCollecte e "
            + "JOIN Signalement s ON e.idSignalement = s.idSignalement "
            + "JOIN Zone z ON s.idZone = z.idZone WHERE z.nomZone = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nomZone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException ex) {
            logger.error("Erreur getNoteMoyenneParZone", ex);
            return 0.0;
        }
    }

    // ============================================================
    // KPIs ADMIN — vue_kpi_zone
    // ============================================================

    public List<KpiZone> getKpiParZone() {
        ensureSchema();
        List<KpiZone> list = new ArrayList<>();
        String query = "SELECT * FROM vue_kpi_zone ORDER BY total DESC";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KpiZone k = new KpiZone();
                k.nomZone               = rs.getString("nomZone");
                k.total                 = rs.getInt("total");
                k.enAttente             = rs.getInt("enAttente");
                k.enCours               = rs.getInt("enCours");
                k.termines              = rs.getInt("termines");
                k.tauxResolution        = rs.getDouble("tauxResolution");
                k.tempsResolutionMoyenH = rs.getDouble("tempsResolutionMoyenH");
                k.urgents               = rs.getInt("urgents");
                list.add(k);
            }
        } catch (SQLException ex) {
            logger.error("Erreur getKpiParZone", ex);
        }
        return list;
    }

    /** Suivi complet d'un signalement pour la vue citoyen. */
    public SuiviSignalement getSuiviComplet(int idSignalement) {
        ensureSchema();
        String query = "SELECT * FROM vue_suivi_signalement WHERE idSignalement = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idSignalement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SuiviSignalement s = new SuiviSignalement();
                    s.idSignalement        = idSignalement;
                    s.statut               = rs.getString("statut");
                    s.nomAgent             = rs.getString("nomAgent");
                    s.heuresEcoules        = rs.getInt("heuresEcoules");
                    s.heuresResolution     = rs.getObject("heuresResolution") != null
                                             ? rs.getInt("heuresResolution") : null;
                    s.noteEvaluation       = rs.getObject("noteEvaluation") != null
                                             ? rs.getInt("noteEvaluation") : null;
                    Timestamp tsAff = rs.getTimestamp("dateAffectation");
                    if (tsAff != null) s.dateAffectation = tsAff.toLocalDateTime();
                    Timestamp tsColl = rs.getTimestamp("dateCollecte");
                    if (tsColl != null) s.dateCollecte = tsColl.toLocalDateTime();
                    return s;
                }
            }
        } catch (SQLException ex) {
            logger.error("Erreur getSuiviComplet #{}", idSignalement, ex);
        }
        return null;
    }

    // ============================================================
    // DATA CLASSES
    // ============================================================

    public static class HistoriqueEntry {
        public int           idHistorique;
        public int           idSignalement;
        public String        ancienStatut;
        public String        nouveauStatut;
        public String        nomAuteur;
        public LocalDateTime dateChangement;
        public String        commentaire;
    }

    public static class EvaluationEntry {
        public int           idSignalement;
        public int           note;
        public String        commentaire;
        public LocalDateTime dateEvaluation;
    }

    public static class KpiZone {
        public String nomZone;
        public int    total;
        public int    enAttente;
        public int    enCours;
        public int    termines;
        public double tauxResolution;
        public double tempsResolutionMoyenH;
        public int    urgents;
    }

    public static class SuiviSignalement {
        public int           idSignalement;
        public String        statut;
        public String        nomAgent;
        public int           heuresEcoules;
        public Integer       heuresResolution;
        public Integer       noteEvaluation;
        public LocalDateTime dateAffectation;
        public LocalDateTime dateCollecte;
    }
}
