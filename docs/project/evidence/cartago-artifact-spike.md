# CArtAgO Artifact API Spike

Status: verified static pilot evidence

## Dependency and API

- Coordinate: `org.jacamo:cartago:3.1`, from the existing JaCaMo raw Maven
  repository.
- JAR SHA-256:
  `bf03b37b28e304b2d784c8d77267e56c12b943ff70eed35b533c6323e0736866`.
- Official tag: `v3.1` in `CArtAgO-lang/cartago`.
- `javap` verifies `cartago.OPERATION`, `cartago.Artifact`,
  `cartago.ObsProperty`, and `cartago.OpFeedbackParam` in the downloaded JAR.
- The official [`OPERATION.java`](https://github.com/CArtAgO-lang/cartago/blob/v3.1/src/main/java/cartago/OPERATION.java)
  uses runtime retention, so reflection is a supported static boundary.
- The official [`Artifact.java`](https://github.com/CArtAgO-lang/cartago/blob/v3.1/src/main/java/cartago/Artifact.java)
  creates observable properties through protected
  `defineObsProperty(String,Object...)`; there is no equivalent static property
  annotation in the inspected API.

## Selected boundary

`CArtAgOArtifactAdapter` reflects annotated methods and immediately converts
them to immutable plugin-owned operation signatures. Observable properties are
supplied as explicit static descriptors with evidence. The adapter does not
instantiate an artifact, create a workspace, call `CartagoService`, parse Java
source, or infer dynamic values.

The CArtAgO JAR is shaded so supplied artifact classes can link to its API; its
POM has no transitives, and package smoke continues to reject Moise. Official
source headers identify the library license as LGPL 2.1 or later; the embedded
third-party notice records this dependency.

## Verification

- `CArtAgOArtifactAdapterTest`: official annotation normalization and invalid
  class rejection.
- `AuctionEnvironmentConsistencyTest`: valid static baseline, dynamic UNKNOWN,
  missing-operation, wrong-arity, wrong-property, trace contribution, and API
  boundary.
- `RuleCatalogCompletenessTest`: 22 standard IDs remain unchanged and the three
  environment IDs are documented.
- Package inspection requires `cartago/OPERATION.class` and
  `cartago/Artifact.class`, while rejecting the Moise marker.
