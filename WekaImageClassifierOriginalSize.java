import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Attribute;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class WekaImageClassifierOriginalSize extends JFrame {

    private JLabel resultLabel;
    private Classifier classifier;
    private Instances dataStructure;
    private int imageWidth, imageHeight;

    public WekaImageClassifierOriginalSize() {
        setTitle("Digit Classifier (Original Size)");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JButton loadModelBtn = new JButton("Load WEKA Model");
        JButton classifyBtn = new JButton("Select Image to Classify");
        resultLabel = new JLabel("Result: ", SwingConstants.CENTER);

        loadModelBtn.addActionListener(this::loadModel);
        classifyBtn.addActionListener(this::classifyImage);

        JPanel panel = new JPanel();
        panel.add(loadModelBtn);
        panel.add(classifyBtn);

        add(panel, BorderLayout.NORTH);
        add(resultLabel, BorderLayout.CENTER);

        setVisible(true);
    }

    private void loadModel(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()))) {
                classifier = (Classifier) ois.readObject();
                resultLabel.setText("Model loaded. Now select an image.");
            } catch (Exception ex) {
                ex.printStackTrace();
                resultLabel.setText("Failed to load model.");
            }
        }
    }

    private void classifyImage(ActionEvent e) {
        if (classifier == null) {
            resultLabel.setText("Please load a model first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage image = ImageIO.read(chooser.getSelectedFile());
                imageWidth = image.getWidth();
                imageHeight = image.getHeight();

                // Rebuild the data structure dynamically based on image size
                prepareHeader(imageWidth, imageHeight);

                double[] instanceValues = new double[imageWidth * imageHeight + 1];
                int index = 0;
                for (int y = 0; y < imageHeight; y++) {
                    for (int x = 0; x < imageWidth; x++) {
                        int pixel = image.getRGB(x, y);
                        int r = (pixel >> 16) & 0xff;
                        int g = (pixel >> 8) & 0xff;
                        int b = pixel & 0xff;
                        int avg = (r + g + b) / 3;
                        int bin = (avg > 100) ? 1 : 0;
                        instanceValues[index++] = bin;
                    }
                }

                instanceValues[index] = Double.NaN; // unknown class value

                DenseInstance instance = new DenseInstance(1.0, instanceValues);
                instance.setDataset(dataStructure);

                double result = classifier.classifyInstance(instance);
                String label = dataStructure.classAttribute().value((int) result);

                resultLabel.setText("Prediction: " + label);
            } catch (Exception ex) {
                ex.printStackTrace();
                resultLabel.setText("Classification failed.");
            }
        }
    }

    private void prepareHeader(int width, int height) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // Create binary attributes for all pixels
        for (int i = 0; i < width * height; i++) {
            ArrayList<String> binaryVals = new ArrayList<>();
            binaryVals.add("0");
            binaryVals.add("1");
            attributes.add(new Attribute("pixel" + i, binaryVals));
        }

        // Add class attribute
        ArrayList<String> classLabels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            classLabels.add("B" + i);
            classLabels.add("E" + i);
        }
        attributes.add(new Attribute("class", classLabels));

        dataStructure = new Instances("digit_classification", attributes, 0);
        dataStructure.setClassIndex(dataStructure.numAttributes() - 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WekaImageClassifierOriginalSize::new);
    }
}
