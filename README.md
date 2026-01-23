Development Setup

Git & GitHub Workflow

We use Git for version control. Follow these steps to ensure a clean revision history:
1. Authentication (One-Time Setup)

GitHub requires a Personal Access Token (PAT) for NetBeans integration.

    Generate a token in GitHub Settings > Developer Settings > Tokens (classic).

    Select the repo scope.

    Use this token as your password when prompted in NetBeans.

2. Standard Session Workflow

To avoid merge conflicts, always follow this order:

    Pull: Right-click project > Git > Remote > Pull. This gets the latest changes from the team.

    Code: Work on your assigned features.

    Commit: Right-click project > Git > Commit. Use a descriptive message (e.g., "Added player jump logic").

    Push: Right-click project > Git > Remote > Push. This sends your changes to the cloud.This is a Readme file
