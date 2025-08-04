# 🧠 Human Anatomy Viewer

A JavaFX-based interactive 3D viewer for exploring human anatomical models — complete with animation, infinite rotation, and AI-assisted search.

---

## 📦 Project Overview

**Human Anatomy Viewer** is a modular JavaFX application designed to visualize 3D anatomical structures. The application supports interactive features like explosion animations, regex + AI search, undo/redo for selections, color customization, and volume-based pie chart analytics.

> Built using **Java 20**, **JavaFX**, and **Maven**, with FXML layout designed in **Scene Builder**.

---

## 🚀 Features

| Feature                | Description                                                                 |
|------------------------|-----------------------------------------------------------------------------|
| 🧩 **3D Model Viewer** | Load and interact with 3D anatomical structures in `.obj` format             |
| 🌀 **Explosion Animation** | Dynamically separate anatomical parts for better inspection                |
| 🎨 **Color Picker**     | Customize part colors manually or with AI suggestions                      |
| 🔍 **AI Search**        | Natural language search (via OpenAI API) with regex fallback                |
| 🎯 **Volume Pie Chart** | Compare part volumes with interactive, zoomable chart                       |
| 🔄 **Undo/Redo**        | Revert or re-apply model visibility and coloring actions                    |
| 🧭 **Infinite Rotation**| Mouse-based camera controls allow smooth 3D orbiting                        |

---

## 🛠 Tech Stack

- ☕ Java 20
- 🖼 JavaFX (3D graphics, UI components)
- 📄 FXML (Scene Builder)
- 🧠 OpenAI GPT (for natural language search)
- 📦 Maven (dependency management)

---

## 🧰 Build & Run Instructions

### ✅ Prerequisites

- Java 20 or later
- Maven 3.6+
- Internet access (for AI search)

### 🔧 Clone & Build

```bash
git clone https://github.com/<your-username>/human-anatomy-viewer.git
cd human-anatomy-viewer
mvn clean install