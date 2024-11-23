package ua.edu.ucu.apps;

import com.google.cloud.vision.v1.*;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SmartDocument implements Document {

    private final String gcsPath;

    @SneakyThrows
    @Override
    public String parse() {
        ImageSource imageSource = ImageSource.newBuilder()
                .setGcsImageUri(gcsPath)
                .build();

        Image image = Image.newBuilder()
                .setSource(imageSource)
                .build();

        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                .build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(Collections.singletonList(request));
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasFullTextAnnotation()) {
                    return res.getFullTextAnnotation().getText();
                }
            }
        }

        return "";
    }
}
