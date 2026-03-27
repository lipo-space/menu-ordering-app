<!--
Sync Impact Report - Constitution v1.0.0
===========================================
Version Change: INITIAL → 1.0.0 (First ratification)
Created: 2026-03-26

Modified Principles: N/A (initial creation)
Added Sections:
  - I. Offline-First Architecture
  - II. Test-Driven Development (TDD) - NON-NEGOTIABLE
  - III. Performance Excellence
  - IV. Minimalist Design Philosophy
  - V. Automated Deployment & Quality Gates
  - Technology Stack Requirements
  - Development Workflow
  - Governance

Removed Sections: N/A (initial creation)

Templates Requiring Updates:
  ✅ plan-template.md - Constitution Check section will reference these 5 principles
  ✅ spec-template.md - Requirements section aligned with offline-first and performance standards
  ✅ tasks-template.md - Test-first workflow enforced, performance validation tasks added
  ⚠ commands/*.md - No command files found (directory does not exist)

Follow-up TODOs: None
-->

# Menu App Constitution

## Core Principles

### I. Offline-First Architecture

All features MUST function without network connectivity. Data synchronization is a background enhancement, not a prerequisite for core functionality.

**Why**: Users in restaurants may encounter poor or no network connectivity. The app must remain usable at all times to support the critical business function of ordering food.

**Requirements**:
- Every feature MUST work offline using local Room database cache
- Network operations MUST NOT block the UI thread
- Data sync MUST happen asynchronously in the background
- Conflict resolution strategy MUST be defined for each sync entity
- Users MUST be notified of sync status (syncing, synced, offline, conflict)
- Local cache MUST be the single source of truth for the UI layer

**How to Apply**: When designing any feature, start with the offline scenario: "How does this work without internet?" Only after validating offline functionality should sync logic be added.

### II. Test-Driven Development (TDD) - NON-NEGOTIABLE

All production code MUST be written following strict Test-Driven Development practices. No exceptions.

**Why**: TDD ensures code quality, prevents regressions, and serves as living documentation. This is a foundational practice that cannot be compromised.

**Requirements**:
- Tests MUST be written BEFORE implementation code
- Tests MUST fail initially (red phase)
- Implementation MUST make tests pass with minimal code (green phase)
- Code MUST be refactored while keeping tests green (refactor phase)
- No pull request may be merged without accompanying tests
- Test coverage MUST be maintained above 80% for business logic
- Unit tests, integration tests, and UI tests are all required

**How to Apply**:
1. Write a failing test that describes expected behavior
2. Run test to confirm it fails
3. Write the minimal implementation to make test pass
4. Refactor code while ensuring tests stay green
5. Repeat for each feature increment

### III. Performance Excellence

Performance is a feature, not an afterthought. The app MUST meet strict performance budgets to ensure exceptional user experience.

**Why**: Performance directly impacts user satisfaction and business success. Slow or janky apps frustrate users and lead to abandonment.

**Requirements**:
- Cold startup time MUST be under 2 seconds (measured from launch to first interactive frame)
- All animations MUST maintain 60 frames per second (16.67ms per frame budget)
- UI operations MUST complete within 16ms to prevent frame drops
- Network timeouts MUST be set appropriately (recommend 30s for slow networks)
- Image loading MUST be asynchronous with progressive rendering
- Database queries MUST be optimized with proper indexing
- Memory usage MUST stay under 150MB under normal operation

**How to Apply**: Profile early and often. Use Android Profiler, Systrace, and benchmarking libraries. Set up performance monitoring in CI/CD. Reject code that violates performance budgets.

### IV. Minimalist Design Philosophy

Every feature, UI element, and line of code must justify its existence. Simplicity is the ultimate sophistication.

**Why**: A minimalist approach reduces cognitive load, decreases maintenance burden, improves performance, and accelerates development velocity.

**Requirements**:
- UI MUST follow Material Design principles with minimal chrome
- Features MUST be justified with clear user value before implementation
- Code MUST avoid over-engineering (YAGNI - You Aren't Gonna Need It)
- Each screen MUST have a single primary action
- Navigation MUST be intuitive with maximum 3 taps to any feature
- Preference MUST be given to platform-standard components over custom implementations
- Code duplication is acceptable up to 3 occurrences before extracting abstractions

**How to Apply**: Before adding any feature or code, ask: "Is this necessary? Can we achieve the same goal with less? What can we remove instead of add?"

### V. Automated Deployment & Quality Gates

All code changes MUST flow through automated pipelines with quality gates. Manual deployment is prohibited.

**Why**: Automation eliminates human error, ensures consistency, enables rapid iteration, and maintains quality standards at scale.

**Requirements**:
- All code MUST be version-controlled in GitHub
- Every pull request MUST pass automated tests before merge
- Merged code MUST automatically deploy to staging environment
- Production deployments MUST be triggered by release tags
- API changes MUST automatically deploy to Vercel
- Database migrations MUST be versioned and reversible
- Rollback procedures MUST be documented and tested

**How to Apply**:
1. Create feature branch from `main`
2. Write tests and implementation
3. Open pull request
4. CI runs tests, linting, and static analysis
5. Code review approval required
6. Merge triggers automatic staging deployment
7. Tag release triggers production deployment

## Technology Stack Requirements

The following technology choices are mandated by this constitution and cannot be changed without amendment:

**Android Application**:
- Language: Kotlin (latest stable version)
- Architecture: MVVM (Model-View-ViewModel)
- Asynchronous Programming: Kotlin Coroutines + Flow
- Local Storage: Room Database
- UI Framework: Jetpack Compose (preferred) or XML Views
- Dependency Injection: Hilt or Koin
- Testing: JUnit, Espresso, MockK

**Backend & Infrastructure**:
- Database: Supabase (PostgreSQL)
- API Hosting: Vercel (Serverless Functions)
- Authentication: Supabase Auth
- Real-time Sync: Supabase Realtime
- File Storage: Supabase Storage (if needed)

**Development Tools**:
- Version Control: Git + GitHub
- CI/CD: GitHub Actions
- Code Quality: Detekt (Kotlin linter)
- Performance Profiling: Android Profiler

## Development Workflow

### Test-First Development Process

1. **Specification Review**: Understand the feature requirement from spec.md
2. **Test Design**: Write acceptance criteria as test cases
3. **Red Phase**: Implement test, confirm failure
4. **Green Phase**: Write minimal code to pass test
5. **Refactor Phase**: Improve code structure while keeping tests green
6. **Review**: Submit PR with all tests passing
7. **Merge**: After approval and CI validation

### Code Review Standards

- All code MUST be reviewed by at least one team member
- Reviewers MUST verify test coverage and quality
- Reviewers MUST check performance implications
- Reviewers MUST validate constitutional compliance
- Automated checks (tests, linting) MUST pass before human review

### Performance Validation

- Startup time benchmarks MUST run in CI
- UI rendering tests MUST validate 60fps maintenance
- Memory leak detection MUST be part of test suite
- Network performance MUST be tested under slow network conditions

## Governance

This constitution supersedes all other development practices and guidelines. Any conflicts between this document and other practices MUST be resolved in favor of the constitution.

### Amendment Process

1. Proposed amendments MUST be documented with clear rationale
2. Impact analysis MUST be performed on existing code/features
3. Migration plan MUST be created if changes affect existing implementation
4. Team consensus (unanimous for core principles, majority for workflow) required
5. Constitution version MUST be incremented per semantic versioning:
   - **MAJOR**: Backward-incompatible principle removal/redefinition
   - **MINOR**: New principle added or material expansion
   - **PATCH**: Clarifications, typo fixes, non-semantic refinements

### Compliance Review

- All pull requests MUST include constitutional compliance checklist
- Quarterly audits MUST review codebase adherence to principles
- Violations MUST be documented with remediation timeline
- Repeated violations trigger architectural review

### Complexity Justification

Any violation of constitutional principles (e.g., exceeding performance budgets, bypassing TDD, adding unnecessary features) MUST be documented in the Implementation Plan's Complexity Tracking table with:
- Specific violation
- Why the violation is necessary
- What simpler alternatives were considered and why they were rejected

**Version**: 1.0.0 | **Ratified**: 2026-03-26 | **Last Amended**: 2026-03-26
