package fingerprints;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Slider;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class Controller implements Initializable{

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        handleScaleSlider();

    }

    public class Pixel {
        public int x;
        public int y;

        public Pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public int getX() {
            return x;
        }
        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    private double imageWidth;
    private double imageHeight;
    private double scale;

    @FXML
    public ImageView imageView;

    @FXML
    public Slider slider;

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
    @FXML
    private void handleScaleSlider() {
        slider.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
            scale = slider.getValue();
            scaleImage();
        });
    }

    private void scaleImage() {
        imageView.setFitHeight(imageHeight * scale);
        imageView.setFitWidth(imageWidth * scale);
    }
    @FXML
    public void handleBinarizationButton(ActionEvent actionEvent) {
        otsuMethod();
    }

    private void otsuMethod() {
        int[] rgbTable = countPixels();
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
        int[] rgbTable = new int[256];

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
                int[] rgbColor = analyzePixel(i, j);
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
    @FXML
    public void handleThinningButton() {
        thinImage();
    }
    @FXML
    public void handleMinutiaeButton() {
        findMinutiae();



    }

    private void findMinutiae() {
        ArrayList<Pixel> minutiaeList = new ArrayList<>();
        for (int i = 4; i < imageWidth - 4; i++) {
            for (int j = 4; j < imageHeight - 4; j++) {
                if (isBifurcation(i, j))
                    minutiaeList.add(new Pixel(i, j));
            }

        }

        removeReplications(minutiaeList);
        WritableImage wi = new WritableImage((int)imageWidth, (int)imageHeight);

        PixelWriter writer = wi.getPixelWriter();

        for ( int i=0;i<imageWidth;i++){
            for (int j=0;j<imageHeight;j++){
                writer.setColor(i,j,imageView.getImage().getPixelReader().getColor(i,j));
            }
        }
        for (Pixel p : minutiaeList){
            writer.setColor(p.x, p.y, Color.RED);
        } 
//        for (int i = 0; i < imageWidth; i++) {
//            for (int j = 0; j < imageHeight ; j++) {
//                if(wi.getPixelReader().getColor(i,j).equals(Color.BLACK))
//                    writer.setColor(i, j, Color.BLACK);
//                else if (wi.getPixelReader().getColor(i,j).equals(Color.WHITE))
//                writer.setColor(i, j, Color.WHITE);
//
//            }
//
//        }

        imageView.setImage(wi);
    }

    private void removeReplications(ArrayList<Pixel> list) {
        for (int i = 0; i < list.size(); i++){
            Pixel p = list.get(i);
            for (int j = 0; j < list.size(); j++) {
                Pixel q = list.get(j);
                if (countDistance(p, q) < 20)
                    list.remove(j);
            }
        }
    }

    private double countDistance(Pixel p, Pixel q) {
        return Math.sqrt((p.x - q.x) * (p.x - q.x) + (p.y - q.y) * (p.y - q.y));

    }

    private boolean isBifurcation(int x, int y) {
        //9x9 square

        //top edge
        int outerSquareCounter = 0;
        for (int i = x - 4; i <= x + 4; i++)
            if (imageView.getImage().getPixelReader().getColor(i, y + 4).equals(Color.BLACK))
                outerSquareCounter++;


        //right edge
        for (int i = y - 4; i <= y + 4; i++)
            if (imageView.getImage().getPixelReader().getColor(x + 4, i).equals(Color.BLACK))
                outerSquareCounter++;

        //bottom edge
        for (int i = x - 4; i <= x + 4; i++)
            if (imageView.getImage().getPixelReader().getColor(i, y - 4).equals(Color.BLACK))
                outerSquareCounter++;

        //left edge
        for (int i = y - 4; i <= y + 4; i++)
            if (imageView.getImage().getPixelReader().getColor(x - 4, i).equals(Color.BLACK))
                outerSquareCounter++;

        if (outerSquareCounter != 3)
            return false;


        //5x5 square

        //top edge
        int innerSquareCounter = 0;
        for (int i = x - 2; i <= x + 2; i++)
            if (imageView.getImage().getPixelReader().getColor(i, y + 2).equals(Color.BLACK))
                innerSquareCounter++;


        //right edge
        for (int i = y - 2; i <= y + 2; i++)
            if (imageView.getImage().getPixelReader().getColor(x + 2, i).equals(Color.BLACK))
                innerSquareCounter++;

        //bottom edge
        for (int i = x - 2; i <= x + 2; i++)
            if (imageView.getImage().getPixelReader().getColor(i, y - 2).equals(Color.BLACK))
                innerSquareCounter++;

        //left edge
        for (int i = y - 2; i <= y + 2; i++)
            if (imageView.getImage().getPixelReader().getColor(x - 2, i).equals(Color.BLACK))
                innerSquareCounter++;

        if (innerSquareCounter != 3)
            return false;
        return true;
    }
    private void thinImage(){
        otsuMethod();
        int[][] imageArray = new int[(int)imageWidth][(int)imageHeight];
        for (int i=0;i<imageWidth;i++){
            for (int j=0;j<imageHeight;j++){
                if(imageView.getImage().getPixelReader().getColor(i,j).equals(Color.BLACK)){
                    imageArray[i][j]=1;
                }
                else{
                    imageArray[i][j]=0;
                }
            }
        }
        ThinningService ts=new ThinningService();
        ts.doZhangSuenThinning(imageArray,true);

        WritableImage wi = new WritableImage((int)imageWidth, (int)imageHeight);

        PixelWriter writer = wi.getPixelWriter();

        for ( int i=0;i<imageWidth;i++){
            for (int j=0;j<imageHeight;j++){
                if(imageArray[i][j]==1){
                    writer.setColor(i,j,Color.BLACK);
                }
                else{
                    writer.setColor(i,j,Color.WHITE);
                }

            }
        }
        imageView.setImage(wi);
    }
}
