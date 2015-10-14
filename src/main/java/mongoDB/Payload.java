package mongoDB;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {
	private List<Body> bodies;

	public List<Body> getBodies() {
		return bodies;
	}

	public void setBodies(List<Body> bodies) {
		this.bodies = bodies;
	}

}
