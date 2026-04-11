RecycleNet ♻️
RecycleNet is an intelligent, real-time Android application designed to classify waste and recyclable materials on-device. Built with modern Android development practices, it leverages Jetpack Compose for a sleek UI, CameraX for seamless video feed handling, and PyTorch Mobile for lightning-fast object detection.

Currently, the model is trained to detect and classify common plastic types: HDPE, LDPE, PET, PP, PS, and PVC.

✨ Features
Live Scanner: Real-time object detection using your device's camera. Draws "tech-style" bounding box corners over detected materials and displays real-time confidence scores.

Static Image Analysis: Capture a new photo or upload an existing image from your gallery to run a detailed inspection.

On-Device Inference: Uses a local PyTorch model (best.ptl), meaning no internet connection is required to classify items, ensuring privacy and speed.

Modern UI: Built entirely with Jetpack Compose and Material Design 3, featuring smooth cross-fade navigation and dynamic progress indicators.

🛠️ Tech Stack
Language: Kotlin

UI Framework: Jetpack Compose (Material 3)

Camera API: CameraX

Machine Learning: PyTorch Mobile (org.pytorch:pytorch_android_lite)

Asynchronous Operations: Kotlin Coroutines & Java Executors

🚀 Getting Started
Prerequisites

Android Studio (Latest stable version recommended)

Android device or emulator running API 24 or higher (a physical device is highly recommended for CameraX and ML performance).

Installation

Clone the repository:

Bash
git clone https://github.com/yourusername/RecycleNet.git
Add the PyTorch Model:
The application relies on a custom trained YOLO model. You must place your exported PyTorch Lite model into the assets folder before building.

Rename your model to best.ptl.

Place it in the following directory: app/src/main/assets/best.ptl.
(Note: If the assets folder does not exist, create it).

Build and Run:
Sync the Gradle files and run the application on your physical device.

🧠 How It Works
Model Loading: The ModelRunner class lazily loads the best.ptl model from the device's storage on first use.

Image Preprocessing: Images (whether from CameraX or the Gallery) are resized to 640x640 and converted into a normalized Float32Tensor as expected by standard YOLO architectures.

Inference & Post-processing: The model outputs a tensor containing bounding box coordinates [x1, y1, x2, y2], confidence scores, and class IDs. The app filters out predictions below the confidence threshold and maps them back to the original image dimensions for the UI overlay.

🤝 Contributing
Contributions, issues, and feature requests are welcome!

Fork the Project

Create your Feature Branch (git checkout -b feature/AmazingFeature)

Commit your Changes (git commit -m 'Add some AmazingFeature')

Push to the Branch (git push origin feature/AmazingFeature)

Open a Pull Request

📄 License
Distributed under the MIT License. See LICENSE for more information.
