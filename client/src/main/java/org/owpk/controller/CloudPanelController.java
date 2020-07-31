package org.owpk.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.owpk.app.Callback;
import org.owpk.app.Config;
import org.owpk.message.MessageType;
import org.owpk.message.Messages;
import org.owpk.network.InputDataHandler;
import org.owpk.service.NetworkServiceFactory;
import org.owpk.service.NetworkServiceInt;
import org.owpk.util.FileInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class CloudPanelController {
  @FXML public TableView<FileInfo> server_panel;
  @FXML public Button back_btn;
  @FXML public Button forward_btn;
  @FXML public Button up_btn;
  @FXML public TextField cloud_text_field;
  @FXML public Button connect_btn;


  private NetworkServiceInt networkServiceInt;
  private MainSceneController mainSceneController;

  private Callback<List<FileInfo>> cloudTableCallback;
  private Callback<String> statusLabelCallback;

  /**
   * метод вызывается при нажатии на кнопку "connect"
   * {@link NetworkServiceFactory} возвращает {@link NetworkServiceInt},
   * который создает подключение клиента к серверу,
   * потоки данных для созданного подключения обслуживает {@link InputDataHandler}
   * команда выполняется в отедльном сервисном потоке, чтобы не мешать UI
   */
  public void connect(ActionEvent actionEvent) {
    Service<Void> ser = new Service<Void>() {
      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {
          @Override
          protected Void call() throws InterruptedException {
            try {
              networkServiceInt = NetworkServiceFactory.getHandler(Config.getDefaultServer());
              networkServiceInt.connect();
              InputDataHandler inputDataHandler =
                  new InputDataHandler(networkServiceInt,
                      cloudTableCallback, statusLabelCallback);
              forwardServerFolders(actionEvent);
              networkServiceInt.onMessageReceived(inputDataHandler);
            } catch (IOException e) {
              networkServiceInt = null;
              throw new InterruptedException();
            }
            return null;
          }
        };
      }
    };
    //выводим информацию в текс лейбл по результату выполнения
    ser.setOnRunning((WorkerStateEvent event) ->
        mainSceneController.status_label.setText("trying to connect " + Config.getDefaultServer() + "..."));
    ser.setOnSucceeded((WorkerStateEvent event) ->
        mainSceneController.status_label.setText("connected: " + networkServiceInt.getName()));
    ser.setOnFailed((WorkerStateEvent event) ->
        mainSceneController.status_label.setText("unable to connect"));
    ser.start();
  }

  /**
   * Отправляет серверу команду {@link MessageType}
   * сервер возвращает List<FileInfo>
   * @throws IOException
   */
  public void forwardServerFolders(ActionEvent actionEvent) throws IOException {
    String path = cloud_text_field.getText();
    if (path == null || path.isEmpty())
      //test
      path = "";
    //path = serverPathHistory.peek().toString();
    if (networkServiceInt == null) {
      connect(actionEvent);
    } else
      sendMessage(new Messages<>(MessageType.DIR, path));
  }

  private void sendMessage(Messages<?> messages) throws IOException {
    ((ObjectOutputStream) networkServiceInt.getOut()).writeObject(messages);
  }

  private void serverRefresh(List<FileInfo> list) {
    server_panel.getItems().clear();
    server_panel.getItems().addAll(list);
    server_panel.sort();
  }

  public void setMainSceneController(MainSceneController mainSceneController) {
    this.mainSceneController = mainSceneController;
  }

  void disconnect() {
    if (networkServiceInt != null) {
      try {
        networkServiceInt.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void init() {
    cloudTableCallback = x -> {
      server_panel.getItems().addAll(x);
      server_panel.sort();
    };
    statusLabelCallback = s -> mainSceneController.status_label.setText(s);
  }
}