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

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // ============================================================
    // HISTORIQUE DES STATUTS
    // ============================================================

    public List<HistoriqueEntry> getHistoriqueBySignalement(int idSignalement) {
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

    public double getNoteMoyenneParZone(String nomZone) {
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
