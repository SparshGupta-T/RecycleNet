# RecycleNet ♻️

**RecycleNet** is an intelligent, real-time Android application designed to classify waste and recyclable materials on-device. Built with modern Android development practices, it leverages Jetpack Compose for a sleek UI, CameraX for seamless video feed handling, and PyTorch Mobile for lightning-fast object detection.

Currently, the model is trained to detect and classify common plastic types: **HDPE, LDPE, PET, PP, PS, and PVC**.

## ✨ Features

* **Live Scanner:** Real-time object detection using your device's camera. Draws "tech-style" bounding box corners over detected materials and displays real-time confidence scores.
* **Static Image Analysis:** Capture a new photo or upload an existing image from your gallery to run a detailed inspection.
* **On-Device Inference:** Uses a local PyTorch model (`best.ptl`), meaning no internet connection is required to classify items, ensuring privacy and speed.
* **Modern UI:** Built entirely with Jetpack Compose and Material Design 3, featuring smooth cross-fade navigation and dynamic progress indicators.

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Camera API:** CameraX
* **Machine Learning:** PyTorch Mobile (`org.pytorch:pytorch_android_lite`)
* **Asynchronous Operations:** Kotlin Coroutines & Java Executors

## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest stable version recommended)
* Android device or emulator running API 24 or higher (a physical device is highly recommended for CameraX and ML performance).

### Installation

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/yourusername/RecycleNet.git](https://github.com/yourusername/RecycleNet.git)
