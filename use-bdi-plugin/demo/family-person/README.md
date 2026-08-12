# Family Person: Person In A Family

## What this demonstrates

- A small UML/OCL model with `Family`, `Person`, and a many-member family link.
- One Jason AgentSpeak person agent with an achievement goal, a plan, and a
  mapped `greet()` operation.
- A deterministic object snapshot that shows `alice` as a member of `family1`.
- Static JaCaMo and Moise organization files kept beside the model for the
  complete project architecture.
- `family-organization.use` and `family-organization.cmd` provide a valid
  standalone organization UML/OCL snapshot when that layer is presented.

This is a valid baseline demo. It does not contain a mutant or an intentional
parser/OCL failure, and it does not start a JaCaMo runtime.

## Recommended GUI flow

1. Open `Family.use`, then execute `Family.cmd`.
2. Choose `View > Create View > Class diagram` to show `Family`, `Person`,
   `FamilyMembers`, attributes, and the OCL invariants.
3. Choose `View > Create View > Object diagram` to show `family1`, `alice`,
   and the family membership link.
4. Choose `Plugins > AgentSpeak > Import JaCaMo Project...` and select
   `family-person.jcm`.
5. Open `Mapping`, click `Load...`, and select `FamilyPerson.bdimap.json`.
6. Open `Diagram`, choose `BDI Plan`, click `Fit`, and show the compact path
   `introduce_family -> introduce_family plan -> greet -> Person::greet()`.
   Use `Focus Goal/Plan` after selecting the goal or plan if needed.
7. Click `Refresh USE Snapshot`, then open `Problems` and
   `Export Current Analysis...` to show the valid baseline result.

The repository-level command and UI guide is
[`docs/guide/guide.md`](../../../docs/guide/guide.md).
