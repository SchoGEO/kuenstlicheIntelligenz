import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jannes on 24.01.2016.
 */
public class RectangleList<Rectangle> extends ArrayList<Rectangle> {
    private String model;  //twoPositionModel , threePositionModel , threePositionModel2CNF , fourPositionModel

    public RectangleList(int size) throws IllegalArgumentException{
        super(size);
    }

    public void setModel(String model){
        List<String> models = Arrays.asList("default", "twoPositionModel", "threePositionModel",
                "threePositionModel2CNF", "fourPositionModel");
        if(!models.contains(model)){
            throw new IllegalArgumentException(
                    "the model needs to be 'twoPositionModel', 'threePositionModel', " +
                            "'threePositionModel2CNF' or 'fourPositionModel'"
            );
        }
        this.model = model;
    }

    public String getModel(){
        return this.model;
    }
}
