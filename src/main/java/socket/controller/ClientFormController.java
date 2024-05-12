package socket.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClientFormController {

    public javafx.scene.control.Button Cameraman;

    @FXML
    private Label lblClientName;

    @FXML
    private VBox msgVbox;

    @FXML
    private TextField txtMessage;

    @FXML
    private ScrollPane scrollPane;

    static boolean openWindow = false;
    private static final double PANE_HEIGHT = 500;
    public AnchorPane emojiPane;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientName ;

    public void initialize(){
        assert scrollPane != null : "fx:id=\"scrollPane\" was not injected: check your FXML file.";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socket = new Socket("192.168.8.100", 3030);
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    System.out.println("Client connected");
                    ServerFormController.receiveMessage(clientName+" is joined.");

                    while (socket.isConnected()){
                        String receivingMsg = dataInputStream.readUTF();
                        receivingMsg(receivingMsg, ClientFormController.this.msgVbox);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();

        this.msgVbox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                scrollPane.setVvalue((Double) newValue);
            }
        });
    }


    public void setClientName(String name) {
        lblClientName.setText(name);
        clientName = name;
    }

    private void receivingMsg(String receivingMsg, VBox msgVbox) {
        String[] parts = receivingMsg.split("#", 2);
        String senderName = parts[0];
        String content = parts.length > 1 ? parts[1] : "";

        VBox messageContainer = new VBox();
        messageContainer.setPadding(new Insets(5, 10, 5, 10));
        messageContainer.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 5;");

        if (content.matches(".*\\.(png|jpe?g|gif)$")) {
            // Display sender's name
            Label nameLabel = new Label(senderName + ":");
            nameLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 5 0;");
            messageContainer.getChildren().add(nameLabel);

            Image image = new Image(content);
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(200);
            imageView.setFitWidth(200);
            imageView.setOnMouseClicked(e -> saveImageToFile(content));
            messageContainer.getChildren().add(imageView);

        } else {  // It's text

            String name = receivingMsg.split(":")[0];
            String msgFromServer = receivingMsg.split(":")[1];


            HBox messageBubble = new HBox();
            messageBubble.setAlignment(Pos.CENTER_LEFT);
            messageBubble.setPadding(new Insets(5, 5, 5, 10));

            Text textName = new Text(name + ": ");
            textName.setStyle("-fx-font-weight: bold; -fx-fill: black;");
            TextFlow textFlowName = new TextFlow(textName);

            Text text = new Text(msgFromServer);
            text.setFill(Color.BLACK);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-background-color: #abb8c3; -fx-font-weight: bold; -fx-background-radius: 20px;");
            textFlow.setPadding(new Insets(5, 10, 5, 10));

            messageBubble.getChildren().addAll(textFlowName, textFlow);

            messageContainer.getChildren().add(messageBubble);
        }

        Platform.runLater(() -> {
            msgVbox.getChildren().add(messageContainer);
        });
    }


    private void saveImageToFile(String imageUrl) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                    new FileChooser.ExtensionFilter("PNG", "*.png"),
                    new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                    new FileChooser.ExtensionFilter("GIF", "*.gif")
            );

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (InputStream is = new URL(imageUrl).openStream();
                     ReadableByteChannel rbc = Channels.newChannel(is);
                     FileOutputStream fos = new FileOutputStream(file)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    private void sendMsg(String msgToSend) {
        if (!msgToSend.isEmpty()){
            if(!msgToSend.matches(".*\\.(png|jpe?g|gif)$")){

                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                hBox.setPadding(new Insets(5, 5, 0, 10));

                Text text = new Text(msgToSend);
                text.setStyle("-fx-font-size: 14");
                TextFlow textFlow = new TextFlow(text);

                textFlow.setStyle("-fx-background-color: #0693e3; -fx-font-weight: bold; -fx-color: white; -fx-background-radius: 20px");
                textFlow.setPadding(new Insets(5, 10, 5, 10));
                text.setFill(Color.color(1, 1, 1));

                hBox.getChildren().add(textFlow);

                HBox hBoxTime = new HBox();
                hBoxTime.setAlignment(Pos.CENTER_RIGHT);
                hBoxTime.setPadding(new Insets(0, 5, 5, 10));
                String stringTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                Text time = new Text(stringTime);
                time.setStyle("-fx-font-size: 8");

                hBoxTime.getChildren().add(time);

                msgVbox.getChildren().add(hBox);
                msgVbox.getChildren().add(hBoxTime);

                try {
                    dataOutputStream.writeUTF(clientName + ":" + msgToSend);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                txtMessage.clear();
            }
        }
    }

    @FXML
    void sendbtnOnAction(ActionEvent event) { sendMsg(txtMessage.getText()); }

    @FXML
    void txtMessageOnAction(ActionEvent actionEvent) {
        sendButtonOnAction(actionEvent);
    }

    private void sendButtonOnAction(ActionEvent actionEvent) { sendMsg(txtMessage.getText()); }

    @FXML
    void CamerabtnOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        configureFileChooser(fileChooser);

        Window window = ((Node) actionEvent.getTarget()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            sendImage(file.toURI().toString());
        }
    }

    private void configureFileChooser(FileChooser fileChooser) {
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
        );
    }

    private void sendImage(String file) {
        Image image = new Image(file);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);
        imageView.setFitWidth(200);

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5,5,5,10));
        hBox.getChildren().add(imageView);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        msgVbox.getChildren().add(hBox);

        try {
            System.out.println(clientName + ":" + file);
            dataOutputStream.writeUTF(clientName + "#" + file);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        ServerFormController.receiveMessage(clientName+ " is left");
    }


}