package common.helpers;

import common.ultilities.PropertiesFile;
import io.qameta.allure.Attachment;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;
import org.testng.Reporter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.VideoFormatKeys.QualityKey;

public class CaptureHelper extends ScreenRecorder {

    //Lấy đường dẫn đến project hiện tại
    static String projectPath = System.getProperty("user.dir") + "/";
    //Tạo format ngày giờ để xíu gắn dô cái name của screenshot hoặc record video
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");

    @Attachment(value = "Page screenshot", type = "image/png")
    public static byte[] saveScreenshotPNG(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public static void captureScreenshot(WebDriver driver, String screenName) {
        PropertiesFile.setPropertiesFile();
        try {
            // Kiểm tra nếu driver null hoặc đã đóng
            if (driver == null) {
                System.out.println("Driver is null, cannot capture screenshot.");
                return;
            }
            PropertiesFile.setPropertiesFile();
            String exportCapturePath = PropertiesFile.getPropValue("exportCapturePath");

            File movieFolder = new File(exportCapturePath);
            if (!movieFolder.exists()) {
                movieFolder.mkdirs();
            } else if (!movieFolder.isDirectory()) {
                throw new IOException("\"" + movieFolder + "\" is not a directory.");
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");

            // Chụp ảnh màn hình
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            String filePath = PropertiesFile.getPropValue("exportCapturePath") + "/" + screenName + "_" + dateFormat.format(new Date()) + ".png";
            FileHandler.copy(source, new File(filePath));

            // Lưu screenshot vào Allure
            saveScreenshotPNG(driver);

            System.out.println("Screenshot saved at: " + filePath);
            Reporter.log("Screenshot taken, saved at: " + filePath, true);
        } catch (Exception e) {
            System.out.println("Exception while taking screenshot: " + e.getMessage());
        }
    }

//    --------------------------
    public static ScreenRecorder screenRecorder;
    public String name;

    public CaptureHelper(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat, Format screenFormat,
                         Format mouseFormat, Format audioFormat, File movieFolder, String name) throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        this.name = name;
    }

    @Override
    protected File createMovieFile(Format fileFormat) throws IOException {
        PropertiesFile.setPropertiesFile();
        String exportVideoMonte = PropertiesFile.getPropValue("exportVideoMonte");

        File movieFolder = new File(exportVideoMonte);
        if (!movieFolder.exists()) {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory()) {
            throw new IOException("\"" + movieFolder + "\" is not a directory.");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
        return new File(movieFolder,
                name + "-" + dateFormat.format(new Date()) + "." + Registry.getInstance().getExtension(fileFormat));
    }

    public static void startRecord(String methodName) throws Exception {
        PropertiesFile.setPropertiesFile();
        String exportVideoATU = PropertiesFile.getPropValue("exportVideoATU");

        File file = new File(exportVideoATU);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;

        Rectangle captureSize = new Rectangle(0, 0, width, height);

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration();
        screenRecorder = new CaptureHelper(gc, captureSize,
                new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey,
                        Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)),
                null, file, methodName);
        screenRecorder.start();
    }

    public static void stopRecord() throws Exception {
        screenRecorder.stop();
    }

}


