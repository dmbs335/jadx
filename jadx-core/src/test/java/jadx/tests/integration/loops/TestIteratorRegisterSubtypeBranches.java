package jadx.tests.integration.loops;

import org.junit.jupiter.api.Test;

import jadx.tests.api.SmaliTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestIteratorRegisterSubtypeBranches extends SmaliTest {

	@Test
	public void test() {
		disableCompilation();
		assertThat(getClassNodeFromSmali())
				.code()
				.doesNotContain("CircularGeofence geofence")
				.doesNotContain("if (geofence.type != 3)")
				.containsOne("Geofence geofence = (Geofence) it.next();")
				.containsOne("convertCircular((CircularGeofence) geofence)")
				.containsOne("convertPolygonal((PolygonalGeofence) geofence)")
				.containsOne("convertLinear((LinearGeofence) geofence)")
				.containsOne("type == 3 ? convertLinear((LinearGeofence) geofence) : null");
	}
}
