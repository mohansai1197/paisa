# Coding standards and ethics

[Back to the file directory](../README.md)

**Status:** agreed working principles for future implementation. Application code has not been created or audited yet.

## Ownership and readable code

- The user leads feature decisions. The assistant handles technical implementation and supporting documentation within the agreed scope.
- Use clear names, small focused functions, and separate files by responsibility. Keep shared components and API access consistent.
- Explain important decisions and non-obvious behavior. Keep comments useful and documentation aligned with the actual code.
- Prefer a complete, understandable main journey over unnecessary dependencies and unfinished features.

## Responsible development

- Follow the event rules, including restrictions or disclosure requirements for AI assistance and prior work.
- Respect licenses and attribution requirements for code, libraries, fonts, images, and other assets. Record required credits when selecting them.
- Use original project work or properly permitted resources. Describe team and AI contributions honestly.
- Use fictional demo data by default. Obtain appropriate authorization before using real personal data, and collect only what a feature needs.
- Keep passwords and API keys out of source code, frontend bundles, screenshots, logs, and submitted documents. Provide configuration examples without real secrets.

## Implementation safeguards

- Validate inputs on the backend and return useful error responses without exposing internal secrets.
- Use parameterized database access and appropriate constraints; avoid constructing SQL from untrusted input.
- When authentication is needed, enforce authorization on the backend and use appropriate password handling rather than plaintext storage.
- Keep database credentials on the backend. Use only the permissions required by the application.
- Build labeled controls, keyboard access, visible focus, readable contrast, and responsive layouts into the main journey.

## Honest verification and presentation

- Run checks suited to the implemented behavior and document their actual results.
- Clearly distinguish planned features, sample data, implemented behavior, and verified behavior.
- Label simulated or mocked integrations as such. Avoid fabricated results, usage figures, performance claims, and impact claims.
- Record known limitations and explain them accurately during the demo.
- For every completed feature, record its purpose, actual source paths, data flow, verification, and presentation notes in the relevant topic file.

## Records to maintain during implementation

- Third-party resources and required credits:
- Data sources and permissions, if applicable:
- Event AI/tool requirements and how we meet them:
- Material limitations and unresolved issues: maintain in `07-testing-and-polish.md`.
