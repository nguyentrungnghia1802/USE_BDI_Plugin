# Frozen Research Terminology

Status: **NORMATIVE FOR THE ROADMAP**

Freeze date: 2026-08-17

These definitions constrain thesis, documentation, code comments, diagrams, and evaluation claims. They follow the language-engineering decomposition in the [literature audit](literature-reuse-audit.md) and the project scope in [research positioning](research-positioning.md).

| Term | Frozen meaning in this project | Required distinction / prohibited implication |
|---|---|---|
| **DSML** | A domain-specific modeling language comprising an abstract syntax, at least one concrete syntax, and semantics/constraints for a delimited domain. | A metamodel alone is not automatically a complete DSML. This plugin is not claimed as a new general JaCaMo DSML. |
| **abstract syntax** | The domain concepts, attributes, relationships, multiplicities, and well-formedness structure independent of a particular notation. | Not the same as a Java parser AST or a displayed diagram. |
| **metamodel** | A model that defines the abstract syntax to which model instances conform. | In roadmap wording, prefer **JaCaMo Consistency Analysis Profile** because this work adapts established concepts for analysis rather than claiming a novel general JaCaMo metamodel. |
| **JaCaMo Consistency Analysis Profile** | The bounded, source-aligned concept and correspondence specification used to normalize supported static JaCaMo evidence for USE consistency analysis. | It is not a complete JaCaMo semantics, authoring DSML, runtime model, or claim of first metamodel. |
| **model instance** | A concrete structure conforming to an abstract syntax/metamodel, such as an imported normalized project/agent/organization representation or a USE UML model/state projection. | A source file may be evidence from which a model instance is normalized; it is not interchangeable with the metamodel. |
| **concrete syntax** | A textual or graphical notation through which model instances are written or presented. AgentSpeak/Moise source is textual concrete syntax; prior DSML notation can be graphical concrete syntax. | A renderer implementation is not itself the abstract syntax. |
| **graphical concrete representation** | A read-only visual projection of the immutable analysis/trace model using stable identities, relationships, layers, and issue state. | It is not an editable semantic source model, parser, validator, runtime monitor, or full graphical editor. “Graphical viewpoint” is an accepted synonym when the projection selects a concern/layer. |
| **static semantics** | Well-formedness and consistency constraints evaluated without executing the modeled JaCaMo system. | Static agreement cannot establish runtime behavior or enactment. |
| **operational semantics** | Rules defining how language constructs execute or change state over time. | The project imports/analyzes static evidence and does not define full AgentSpeak/JaCaMo operational semantics. Bounded USE effects are an analysis mechanism, not JaCaMo operational semantics. |
| **correspondence** | An explicit relationship asserting that a source concept and a USE UML/OCL element are intended to represent related domain meaning. | Correspondence does not by itself prove equality or semantic equivalence. |
| **mapping candidate** | A suggested correspondence produced from deterministic evidence/heuristics but not accepted as authoritative by the user/reviewer. | Candidates must not enable rules that require confirmed mappings and must not be displayed as confirmed truth. |
| **confirmed mapping** | A persisted or in-memory correspondence explicitly accepted under the mapping contract, with source/target identity and relevant evidence. | Confirmation authorizes scoped rule evaluation; it does not guarantee the mapping is semantically correct forever. Staleness remains possible. |
| **consistency rule** | A deterministic, identified check with declared prerequisites that evaluates normalized source evidence, mappings, and/or immutable USE evidence to produce a bounded outcome. | A rule is not a theorem prover or whole-system verifier. Missing prerequisites produce the documented failure/UNKNOWN behavior rather than invented PASS. |
| **consistency** | Satisfaction of the declared rule(s) for the supported subset and the available evidence at the analyzed snapshot. | Never use as an unqualified synonym for general system correctness, behavioral equivalence, or runtime validity. |
| **traceability** | Stable, navigable relationships among source elements, mappings, UML/OCL targets, rules, issues, and evidence. | A UI hyperlink alone is not sufficient if the underlying identity/relation/evidence is absent. |
| **evidence** | Bounded, reviewable facts that justify an outcome, such as a source span, mapping identity, UML/OCL reference, evaluated result, fingerprint, or diagnostic detail. | Evidence must not contain invented runtime facts; reports preserve rather than recompute it. |
| **certainty** | The epistemic classification of a finding or trace relation: `CONFIRMED`, `POTENTIAL`, or `UNKNOWN`. | Certainty is distinct from severity and PASS/FAIL-like status. `UNKNOWN` is never PASS. |
| **static JaCaMo project** | A `.jcm` composition and its statically resolved agent/resource/organization declarations normalized without launching JaCaMo. | Does not imply lifecycle, scheduling, live workspace, organization enactment, or runtime trace support. |
| **runtime integration** | Starting, connecting to, controlling, or observing an executing JaCaMo/Jason/CArtAgO/Moise system and its lifecycle/state. | Explicitly out of scope. Having runtime libraries on the plugin classpath for parsing does not constitute runtime integration. |
| **enactment** | The runtime realization of an organizational specification through active agents, memberships, missions, obligations/permissions, and their changing state. | Static Moise parsing/cardinality comparison is not enactment. |
| **live state** | Values and relations observed from a currently executing environment/system rather than statically declared or loaded into a reviewed fixture. | A USE snapshot is current USE analysis state, not automatically live JaCaMo/CArtAgO/Moise state. |
| **USE snapshot** | An immutable projection of the loaded USE UML/OCL specification and current object/link state, identified by a fingerprint for one analysis. | Findings may be snapshot-dependent; snapshot analysis does not imply continuous monitoring. |
| **bounded effect** | A controlled, disposable/restored USE state variation evaluated for a declared check with before/after fingerprint safety. | It is not persistent mutation, simulation of all executions, or a JaCaMo runtime run. |

## Required wording patterns

Use qualifiers when evidence is bounded:

- “for the supported static subset”;
- “for the reviewed Auction corpus”;
- “without starting the JaCaMo runtime”;
- “using explicit confirmed mappings”;
- “when required USE snapshot evidence is available.”

Use “read-only graphical concrete representation” or “traceability-derived graphical viewpoint” for the Diagram tab. Use “analysis profile” for the Task 04 abstract-syntax artifact. Reserve “runtime,” “enactment,” and “live state” for actual execution/observation capabilities, which this scope does not provide.

## Consistency outcome vocabulary

| Dimension | Values | Meaning |
|---|---|---|
| Rule status | Project-defined issue/pass/unknown result states | Whether a declared check succeeded, found a violation, or could not decide under its contract. |
| Severity | `ERROR`, `WARNING`, `INFO` as applicable | Impact/attention level, not confidence. |
| Certainty | `CONFIRMED`, `POTENTIAL`, `UNKNOWN` | Strength/availability of evidence, not severity. |
| Mapping state | candidate, confirmed, stale, missing, unknown as applicable | Lifecycle/quality of a correspondence, not a rule outcome. |

These axes must not be collapsed. In particular, an informational `UNKNOWN` is not a confirmed PASS, and a confirmed mapping can later become stale.
