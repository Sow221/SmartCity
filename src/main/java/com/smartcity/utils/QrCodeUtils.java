package com.smartcity.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Génère des QR codes sous forme d'Image JavaFX.
 */
public class QrCodeUtils {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeUtils.class);

    /**
     * Génère un QR code JavaFX Image pour l'URL donnée.
     * @param url  contenu du QR code
     * @param size taille en pixels (carré)
     * @return Image JavaFX ou null en cas d'erreur
     */
    public static Image generateQrCode(String url, int size) {
        if (url == null || url.isBlank()) return null;
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.MARGIN, 1,
                EncodeHintType.CHARACTER_SET, "UTF-8"
            );
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size, hints);
            WritableImage image = new WritableImage(size, size);
            PixelWriter pw = image.getPixelWriter();
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    pw.setColor(x, y, matrix.get(x, y)
                        ? javafx.scene.paint.Color.BLACK
                        : javafx.scene.paint.Color.WHITE);
                }
            }
            return image;
        } catch (WriterException e) {
            logger.error("Erreur génération QR code pour: {}", url, e);
            return null;
        }
    }
}
