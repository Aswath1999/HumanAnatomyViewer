## Assignment 01:

### **General Remarks**

* The project structure is quite disorganized. For example, your `HelloWorld` class is not inside any package, so it won't run as-is.
* Please do **not create a completely new project for each assignment**. Instead, create **one overall project** and organize your assignments within it, like this:

  ```
  project-name/
    ├── assignment1/
    ├── assignment2/
    └── ...
  ```
* Your package naming is also incorrect. You cannot name a package `Anatomy` (starting with an uppercase letter), and if you do name it that way, using a lowercase version (`anatomy`) in your code will lead to errors and will not be recognized by the compiler.

### 🧩 Add `module-info.java` to Your Project

Please add a `module-info.java` file to your project. This helps Maven manage dependencies and run the application correctly — especially when using JavaFX.

Once added, you won’t need to manually run your project using `mvn` from the command line.

Here’s an example you can use:

```java
module advanced_java_for_bioinformatics2025_Aswath1999 {
    requires javafx.controls;
    requires javafx.graphics;
    // requires javafx.fxml; // Uncomment if using FXML
    exports Anatomy;
}
```

Make sure the module name matches your project structure, and update the `exports` statement to reflect the correct package you want to expose.
---

### **Task 2**

* **Design:** OK
* **Functionality:** OK
* The structure is messy, but I won’t deduct points this time.
* **Points:** **2/2**

---

### **Task 3**

* **Design:**

  * The overall structure needs significant improvement.
  * Your program is not able to handle multiple-word inputs (e.g. `"bone" "head"`). It only works with a single word or argument.
  * Package names in your classes are incorrect and inconsistent.
  * The total number of relations and nodes in your output is incorrect. It looks like you're counting all lines directly from the file, including the **first line**, which is just a header. Please make sure to **exclude the header line** when calculating the number of relations and nodes. Keep this in mind for the next assignments.

* **Functionality:**

  * The code logic is there, but the program does not work as expected.
  * With better structure and some adjustments, this could be much improved.

* **Points:** **6/8**

---

### **Total: 8/10**

---

### 🔧 **Action Items**

* Please add a `README.md` file to your GitHub repository with your name and a short description of the project — this is important.
* Also, are you working in a group? If so, please include all group members in the README.

Please work in groups of two for this assignment.
Find a group partner and make sure to add both of your names to the README.md file in your Git repository.



### 💬 Code Comments

Please make sure to **add meaningful comments** throughout your code — as much as possible.
With tools like LLMs (e.g., ChatGPT or GitHub Copilot), it's easier than ever to generate good comments, so there’s no excuse to skip them.

That said, please also **read and review the comments these tools generate** — make sure they actually match your code logic.

Well-commented code makes it much easier for me (and others) to review and understand your work.


---

## 📘 Assignment 02 – Feedback

---

### **Task 1**

* **Implementation:** The implementation is functionally correct.
* **Functionality:** OK
* **Points:** ✅ **4/4**

---


### **Task 2**

* **Design:** The UI is mostly fine, but there are **multiple design and layout issues** that need to be addressed:

#### ❌ Issue 1: Bye button is placed incorrectly

It currently appears in the **top toolbar** alongside the Expand and Collapse buttons, which goes against the assignment's layout requirements.

✅ **Suggested fix:** The Bye button should be moved to the **bottom** using a `ButtonBar`, like this:

```java
ButtonBar buttonBar = new ButtonBar();
buttonBar.getButtons().add(byeBtn);
root.setBottom(buttonBar);
```

🛠️ *I have already added this correction to your code.*

* **Points:** ✅ **1/2**


---

### **Task 3**

* **Implementation:** The implementation works, but the use of **hardcoded file paths** (`"data/partof_parts_list_e.txt"`, etc.) inside `AnatomyDataExplorer` is not ideal. These should be:

  * Moved to `TreeLoader`, or
  * Passed via command-line arguments, or
  * Loaded using `getClass().getResourceAsStream(...)` from the `resources` folder.

  This will make your code **more portable and maintainable**.
* **Functionality:** OK
* **Points:** ✅ **4/4**

---

### **Task 4**

* **Design:** The layout and event-handling logic are appropriate. You correctly use a `ListView` to display `fileIds()` when a node is selected — exactly what the assignment asked for.
* **Functionality:** OK
* **Points:** ✅ **3/3**

---

### **Task 5**

* **Design:** The helper methods like `expandAll()` and `collapseAll()` are good, and your use of event handlers is functional.

* **Functionality:** Several functional issues were present:

#### ❌ Issue 2: Collapse All does not fully collapse

You call `rootItem.setExpanded(true)` immediately after collapsing, which re-expands the root and cancels the visual effect of the collapse. sometimes it shows all the file ids. 
✅ *I removed that line so the full tree properly collapses.*

#### ❌ Issue 3: Expand All keeps previous selection

When clicking "Expand All", the previously selected node remains highlighted and its file IDs stay visible in the `ListView`, even though the context has changed.

✅ **Suggested fix (already added):**

```java
treeView.getSelectionModel().clearSelection();
listView.getItems().clear();
```

#### ❌ Issue 4: Clicking **Collapse All** shows **every file ID** in the list view

This is **not expected behavior** — the `ListView` should only show file IDs for the **currently selected node**, and after collapsing the tree, **no node should remain selected**.

But in your original code, the selection is not cleared, so the previously selected node stays active, and its file IDs continue to appear — which gives the impression that "everything" is being shown.

🛠️ *I fixed this by clearing the selection and the `ListView` when collapsing the tree:*

```java
treeView.getSelectionModel().clearSelection();
listView.getItems().clear();
```

This ensures the UI reflects the collapsed state accurately.

---

### ✅ Points for Task 5: **1/2**

---

### ✅ **Total: 13/15**



---

### 📌 Final Reminder

* ❓ If **any part of the assignment or feedback is unclear**, please feel free to ask during the tutorial or email me directly.
* ⚠️ Avoid hardcoding file paths. Use:

  ```java
  getClass().getResourceAsStream("/assignment02/partof_parts_list_e.txt")
  ```

  or similar logic to load from the `resources` folder. This improves cross-platform compatibility and project cleanliness.


