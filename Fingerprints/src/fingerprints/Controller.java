package fingerprints;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;



public class Controller {

    private double imageWidth;
    private double imageHeight;

    @FXML
    private ImageView imageView;
    @FXML

    public void handleLoadImageButton() {
        loadImage();
    }

    public void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.jpg", "*.png", "*.bmp"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );

        File file = fileChooser.showOpenDialog(new Stage());

        try {
            String imagePath = file.getPath();
            WritableImage image = SwingFXUtils.toFXImage(ImageIO.read(new File(imagePath)), null);

            imageView.setImage(image);
            imageView.setFitWidth(image.getWidth());
            imageView.setFitHeight(image.getHeight());

            imageView.setFitHeight(image.getHeight());
            imageView.setFitWidth(image.getWidth());

            imageHeight = image.getHeight();
            imageWidth = image.getWidth();

        } catch (NullPointerException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("No file selected");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("File can't be opened");
            alert.showAndWait();
        }

    }

    public void handleSaveImageButton() {
        saveImage(imageView.getImage());
    }

    private void saveImage(Image image) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(new Stage());
        fileChooser.setTitle("Save image");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR");
            alert.setHeaderText("File can't be saved");
            alert.showAndWait();
        }

    }

    public void handleBinarizationButton(ActionEvent actionEvent) {
        otsuMethod();
    }

    private void otsuMethod() {
        int [] rgbTable = countPixels();
        int total = (int) imageHeight * (int) imageWidth;
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * rgbTable[i];
        }

        float sumB = 0;
        int wB = 0;
        int wF;

        float max = 0;

        int threshold = 0;
        for (int i = 0; i < 256; i++) {
            wB += rgbTable[i];
            if (wB == 0)
                continue;
            wF = total - wB;
            if (wF == 0)
                break;
            sumB += (float) i * rgbTable[i];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            float between = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (between > max) {
                max = between;
                threshold = i;
            }
        }
        createBinaryImage(threshold);
    }
    private int[] countPixels() {
        int []rgbTable = new int[256];

        for (int i = 0; i < 256; i++) {
            rgbTable[i] = 0;
        }
        for (int x = 0; x < imageView.getImage().getWidth(); x++) {
            for (int y = 0; y < imageView.getImage().getHeight(); y++) {
                int argb = imageView.getImage().getPixelReader().getArgb(x, y);
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = (argb) & 0xFF;

                rgbTable[(red + green + blue) / 3]++;

            }
        }

        return rgbTable;
    }
    private void createBinaryImage(int threshold) {

        WritableImage wi = new WritableImage((int) imageWidth, (int) imageHeight);
        PixelWriter writer = wi.getPixelWriter();
        Color color;

        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                int[] rgbColor = analyzePixel(i, j);kon
                if (rgbColor[3] <= threshold)
                    color = Color.BLACK;
                else
                    color = Color.WHITE;

                writer.setColor(i, j, color);
            }
        }

        imageView.setImage(wi);

    }
    private int[] analyzePixel(int x, int y) {
        int pixel = imageView.getImage().getPixelReader().getArgb(x, y);
        int[] rgbTable = new int[4];
        rgbTable[0] = (pixel >> 16) & 0xFF;
        rgbTable[1] = (pixel >> 8) & 0xFF;
        rgbTable[2] = (pixel) & 0xFF;
        rgbTable[3] = (rgbTable[0] + rgbTable[1] + rgbTable[2]) / 3;

        return rgbTable;
    }

    public void handleThinningButton(ActionEvent actionEvent) {
    }

    public void handleMinutiaeButton(ActionEvent actionEvent) {
    }
}
