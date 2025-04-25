# Listsqre Revamped

Listsqre is an Android application built with Jetpack Compose that allows users to create, manage, and organize task cards. It features an interactive UI with functionalities such as adding, editing, deleting, and reordering tasks. Additionally, it provides notification scheduling for reminders.

## Features
- Create and manage task cards
- Edit task names
- Delete checked tasks
- Reorder/Pin tasks (move checked items to the top)
- Schedule notifications with a time picker
- Modern Material 3 UI with Jetpack Compose

## Tech Stack
- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI Toolkit
- **Room Database** - Local storage for tasks
- **Material3 Components** - UI styling
- **Coroutines** - Asynchronous task management

## Installation
1. Clone this repository:
   ```sh
   git clone https://github.com/zhanruifam/Listsqre_v2.git
   ```
2. Open the project in Android Studio.
3. Sync dependencies and build the project.
4. Run the app on an emulator or physical device.

## Usage
1. **Adding a Task**
    - Tap the `+` button at the bottom to create a new task.
2. **Editing a Task**
    - Tap the edit icon next to a task to modify its name.
3. **Deleting Completed Tasks**
    - Select tasks using checkboxes and use the menu option to delete checked tasks.
4. **Reordering/Pinning Tasks**
    - Use the menu option to move checked items to the top.
5. **Setting Notifications**
    - Select "Notify" from the menu and choose a time for reminders.

## Permissions
- The app requests the `POST_NOTIFICATIONS` permission for scheduling reminders.

## Support & Feedback
- If you encounter issues or have suggestions, please open an issue or contact the developer.