package dbast.prometheus.engine.entity.components;

import java.util.Hashtable;
import java.util.Stack;

// TODO make it more complex. Entities should be able to have multiple active states? Might be a different component even
public class StateComponent extends Component {
    private String state;
    public Stack<String> states;
    // currently only used for debugging an animation purposes (might be more useful later)
    private float currentAge;

    public StateComponent() {
        super();
        this.states = new Stack<>();
        this.currentAge = 0f;
        this.setState("default");
    }

    public String getState() {
        return state;
    }

    public void dropState(String state) {
        this.states.remove(state);
        resetState();
    }
    public void dropCurrentState() {
        this.states.remove(this.state);
        resetState();
    }
    private void resetState() {
        if (this.states.isEmpty()) {
            this.states.push("default");
        } else {
            this.state = this.states.peek();
        }
        this.currentAge = 0f;
    }
    public void setState(String state) {
        this.states.push(state);
        this.state = this.states.peek();
        this.currentAge = 0f;
    }

    /**
     * Updates the current state without affecting age. Should only be used for minor modifications to state variants
     * @param updatedState new state
     */
    public void updateCurrentState(String updatedState) {
        this.states.remove(this.state);
        this.state = updatedState;
        this.states.push(this.state);
    }

    public float getCurrentAge() {
        return currentAge;
    }

    public void updateCurrentAge(float updateTime) {
        this.currentAge += updateTime;
    }

}
