package dbast.prometheus.engine.entity.components;

// TODO make it more complex. Entities should be able to have multiple active states? Might be a different component even
public class StateComponent extends Component {
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
