package org.owpk.controller;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.owpk.util.FileInfo;

public class TypeImageCell<T> extends TableCell<T, FileInfo.FileType> {
  private final ImageView image;

  public TypeImageCell() {
    image = new ImageView();
    image.setFitWidth(20);
    image.setFitHeight(20);
    image.setPreserveRatio(true);
    setGraphic(image);
    setMinHeight(20);
  }

  @Override
  protected void updateItem(FileInfo.FileType type, boolean empty) {
    super.updateItem(type, empty);
    if (empty || type == null)
      image.setImage(null);
    else {
      image.setImage(new Image(type.getUrl()));
    }
  }
}