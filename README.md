# BrightCare Medical Centre

Distributed System Group Assignment regarding BrightCare Medical Centre.

## Project SOP for Coding

To keep the project organized and easy to maintain, please follow these standard operating procedures when working on the codebase:

1. **Understand the task first**
   - Read the issue, requirement, or assignment instruction carefully.
   - Identify the module or feature you need to change before editing code.

2. **Check the existing code structure**
   - Review related files and follow the current project style.
   - Reuse existing classes, methods, and naming conventions where possible.

3. **Work on a separate branch**
   - Do not code directly on the main branch.
   - Create a new branch for each feature, fix, or improvement.

4. **Make small and clear changes**
   - Keep commits focused on one task at a time.
   - Avoid mixing unrelated changes in the same commit.

5. **Test before pushing**
   - Run the project and verify that your changes work as expected.
   - Fix errors and warnings before creating a pull request.

6. **Write meaningful code comments only when needed**
   - Comment complex logic, but avoid obvious comments.
   - Keep code readable and self-explanatory.

7. **Review your work before submitting**
   - Check formatting, imports, and naming.
   - Make sure there are no debug prints or temporary code left behind.

8. **Coordinate with your group members**
   - Inform the team if you are editing shared files.
   - Avoid overwriting someone else’s work.

## Git Command Guidelines for New GitHub Users

Here are some basic Git commands that can help new contributors work safely and confidently:

### 1. Clone the repository
```bash
git clone <repository-url>
```
Downloads the project to your local machine.

### 2. Check your current branch
```bash
git branch
```
Shows which branch you are currently on.

### 3. Create a new branch
```bash
git checkout -b feature/your-branch-name
```
Creates and switches to a new branch for your work.

### 4. Check file status
```bash
git status
```
Shows which files are changed, staged, or untracked.

### 5. Add changes to staging
```bash
git add .
```
Stages all modified files. You can also add a single file:
```bash
git add README.md
```

### 6. Commit your changes
```bash
git commit -m "Describe your change clearly"
```
Saves your changes locally with a useful message.

### 7. Pull the latest changes
```bash
git pull origin main
```
Updates your local branch with the latest remote changes.

### 8. Push your branch
```bash
git push origin feature/your-branch-name
```
Uploads your branch to GitHub.

### 9. Create a pull request
- Open GitHub in your browser.
- Compare your branch with `main`.
- Add a clear title and description for your changes.

### 10. Useful tips
- Commit frequently with clear messages.
- Pull updates before starting new work.
- Ask for help if you are unsure about merge conflicts.

## Suggested Git Workflow

```bash
git checkout -b feature/new-task
git status
git add .
git commit -m "Add new task implementation"
git pull origin main
git push origin feature/new-task
```

This workflow helps keep the project clean and reduces merge conflicts.
