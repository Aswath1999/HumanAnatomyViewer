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
