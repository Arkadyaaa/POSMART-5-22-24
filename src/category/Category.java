package category;

import javafx.scene.image.Image;

public class Category {
    private String name;
    private Image image;

    // Constructor
    public Category(String name, Image image) {
        this.name = name;
        this.image = image;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for image
    public Image getImage() {
        return image;
    }

    // Setter for image
    public void setImage(Image image) {
        this.image = image;
    }
}
