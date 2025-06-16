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


* **Points:** ✅ **1/2**

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

---

## 📘 Assignment 03 – Feedback

---

### **Task 1**

**Design:**
All required components were implemented, but the **structure is not fully correct**.

You should:

* Start with an `AnchorPane`, and place a `BorderPane` inside it.
* The `MenuBar → Menu → MenuItem` structure should be directly added to the top of the `BorderPane`.
* `SplitPane` should **not** be placed inside an `HBox`.
* Instead, insert the `SplitPane` into a ** same `BorderPane`**, and ensure it resizes correctly when the window size changes.

These layout issues suggest a misunderstanding of JavaFX’s scene graph. Also, **there was no image provided for the scene graph**, although the description suggests one should be there.

I’m not heavily penalizing this since it’s part of the learning curve — but make sure it’s fixed in future projects.

**Points:** 2/3

---

### **Task 2**

**Implementation:**
Your implementation is mostly correct.

However, this method:

```java
public static ArrayList<String> extractWords(TreeItem<ANode> item) {
    ArrayList<String> words = new ArrayList<>();
    extractRecursive(item, words);
    return words;
}
```

...and its helper `extractRecursive(...)` are **better suited to a utility class or `WordCloudItem`**, not `Model`. Keeping separation of concerns will make your code easier to maintain.

**Points:** 1/1

---

### **Task 3**

**Implementation:**
Works fine.

**Functionality:**
Also correct.
However — your **answer to theory question 3.3 is missing**.

**Points:** 1/2

---

### **Task 4**

**Implementation & Functionality:**

✅ Your implementation works, and all required functionality is present.

⚠ However:

* The keyboard shortcuts seem **Windows-specific** — consider supporting `Cmd` on macOS too using JavaFX cross-platform key handling.

* ❗ Logic issue with **Expand All + Select All**:

  > Example: Select “mouth” → Expand All → Select All →
  > It only selects all after **two clicks**, and sometimes **includes unintended parents**.

  This suggests the TreeView’s selection model is shifting automatically during expansion/collapse. You’ll need to investigate this further and make the selection behavior more robust.

---

**Fix applied to `handleSelectAll()`**

You correctly replaced:

```java
if (selected.isEmpty()) {
    // ...
} else {
    selectionModel.clearSelection();
    for (TreeItem<ANode> item : selected) {
        List<TreeItem<ANode>> descendants = new ArrayList<>();
        collectDescendants(item, descendants);
        descendants.forEach(selectionModel::select);
    }
}
```

✅ With this version:

```java
public void handleSelectAll() {
    var selectionModel = treeView.getSelectionModel();
    List<TreeItem<ANode>> selectedItems = new ArrayList<>(selectionModel.getSelectedItems());

    selectionModel.clearSelection(); // ❗ Clear AFTER copying selection

    for (TreeItem<ANode> item : selectedItems) {
        selectionModel.select(item); // ✅ Re-select the originally selected node

        List<TreeItem<ANode>> descendants = new ArrayList<>();
        collectDescendants(item, descendants);

        for (TreeItem<ANode> descendant : descendants) {
            selectionModel.select(descendant); // ✅ Select all children
        }
    }
}
```

This is the correct fix — well done.

📌 Other students: please fix this in your submissions.

**Points:** 3/3

---

### **Task 5**

**Design:**
All required menu items are present and correctly implemented.

**Points:** 1/1

---

### ✅ **Total: 8/10**

Good effort overall.
I'm pointing out these issues for learning purposes — only minor points were deducted here.

⚠ **But be careful** — your **project submission** will be reviewed by **three evaluators**, and issues like structure, missing theory, and improper controller logic **can affect your grade**.

Use this feedback to polish and correct things early. Let me know if you'd like support reviewing your controller, FXML, or interaction logic again.


---

## **Assignment 04**

**General Comments:**

* `ANode` should **not** implement `equals()` or `hashCode()`.

---

#### **Task 1**

* **Design & Functionality:**
  Everything is there as expected.
  **Points: 2/2**

---

#### **Task 2**

* **Functionality:**
  It works fine.
  **Points: 4/4**

---

#### **Task 3**

* **Design:**
  It’s fine.
* **Functionality:**
  It’s fine.
  **Points: 4/4**

---

**Total: 10/10**

**Note:**
Nice you implemented Newick support.

---

## **Assignment 05**

**General Comments:**

* `ObjParser` should be in the **model** package. While it uses JavaFX classes, it only handles parsing and not UI logic.
* `Axes` should be in the **window** package, since it is a JavaFX `Node`.
* The shortcut key doesn’t match the intended action.
* There's a zoom-in limit — after a certain point, the object disappears.
* The implementation still needs various improvements.

---

#### **Task 1**

* **Design & Functionality:**
  The implementation works as required.
  **Points: 4/4**

---

#### **Task 2**

* **Functionality:**
  It works as expected.
  **Points: 3/3**

---

#### **Task 3**

* **Functionality:**
  It’s fine.
  **Points: 1/1**

---

#### **Task 4**

* **Functionality:**
  It works, but there are issues:

  * When loading a new `.obj`, the previous object is **not cleared**.
  * Sometimes objects overlap due to loading from the same position.
  * The scene should be **reset**, and the new object should be **centered**.

    **Points: 2/2**

---

**Total: 10/10**


---

## Assignment 06

* `ObjParser` should ideally be part of the **model** package.
* `setupMouseRotation` could be separated into its own class, e.g., `MouseRotate`, for better modularity.
* I couldn’t find explanations for **Task 3** and **Task 5**. I’ll update the general comment and restore the deducted points once you point me to where they are.

---

### **Task 1**

**Implementation:**
Everything has been implemented as required.

**Points:** 1/1

---

### **Task 2**

**Functionality:**
Works as expected.

**Points:** 2/2

---

### **Task 3**

**Functionality:**
Works fine overall, but the zoom functionality has issues. When zoomed in too far, it's difficult to zoom out again unless you press reset.
You should consider improving this using a structure like:

```java
double yScroll = e.getDeltaY();
double xScroll = e.getDeltaX();

// On macOS, Shift usually modifies scroll behavior
if ((!e.isShiftDown() && isMac) || (!e.isControlDown() && !isMac)) {
    camera.setTranslateZ(camera.getTranslateZ() + yScroll);
} else {
    camera.setTranslateX(camera.getTranslateX() - xScroll);
    camera.setTranslateY(camera.getTranslateY() - yScroll);
}
```

Adjust this logic to match your existing code.

**Points:** 1/2

---

### **Task 4**

**Functionality:**
Also works fine.

**Points:** 2/2

---

### **Task 5**

**Functionality:**
Good job overall.  Functionality is implemented.
(Still, please provide an explanation of this task in your submission.)

**Points:** 2/3

---

### **Total: 8/10**

Great work overall! 

