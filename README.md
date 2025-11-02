# 🧠 Googol Search Engine

A distributed search engine built in Java, designed to simulate the architecture of a real-world system such as Google.  
The project was developed for the **Distributed Systems** course (FCTUC, 2023/2024) and implements features such as web crawling, indexing, relevance ranking, fault tolerance and API integration.

## 👥 Authors
- **Marco Lucas** — 2021219146  
- **Rui Ribeiro** — 2021189478  
- **João Lopes** — 2020236190

## 🧩 Project Overview

**Googol** is a **distributed and fault-tolerant search engine** composed of several modules that communicate via **Java RMI** and **TCP sockets**.  
It simulates the key components of a search infrastructure:

- **RMIGateway** → central point of contact for clients (search requests, indexing new URLs, statistics, etc.)
- **RMIBarrel** → distributed indexers that store inverted indexes and handle search queries.
- **UrlQueue** → manages a queue of URLs to be downloaded and indexed.
- **Downloader** → retrieves web content and sends it to the barrels for processing.
- **RMIClient** → user interface (command-line or web frontend) that allows users to perform searches or submit new URLs.
- **External APIs** → integration with YouTube and HackerNews for external search results.

## ⚙️ Technologies Used

- **Java (RMI, Threads, Sockets)**
- **JSoup** (for web page parsing and HTML extraction)
- **Google API Client** (for YouTube API integration)
- **JSON Simple** (for HackerNews API parsing)
- **Gradle / VSCode / IntelliJ IDEA** (IDE support)

## 📁 Project Structure
src/
├── RMIInterface/ # RMI interfaces for Gateway and Barrel
├── RMIGateway/ # Gateway (entry point and load balancer)
├── RMIBarrel/ # Indexing nodes (store URLs, keywords, links)
├── RMIClient/ # Command-line or web client
├── UrlQueue/ # Queue system for URL management
├── Downloader/ # Download worker that fetches and parses content
├── WebServer/ # Spring/REST API (optional web frontend)
│ ├── YoutubeAPI/
│ └── HackerNewsAPI/
└── Configuration.java # Central configuration parameters


## 📦 Required Libraries

All external dependencies are stored in the `lib` folder.  
Before compiling, make sure the following JARs are added to your project's classpath:

- `jsoup-1.17.2.jar`
- `jsoup-1.17.2-javadoc.jar`
- `jsoup-1.17.2-sources.jar`

## 🚀 How to Run

To ensure correct startup order and avoid RMI connection issues, components **must be started in the following sequence**:

### 1️⃣ UrlQueue
Initializes the TCP queue system for new URLs.

### 2️⃣ RMIGateway
Central node for client and barrel communication.

### 3️⃣ RMIBarrel
At least one barrel must be running (can scale horizontally).  
**Important**: RMIBarrel requires an integer argument as barrel ID.

### 4️⃣ RMIClient
Used to perform searches and submit URLs.

## 🖥️ Running in VS Code

1. Open the project in **VSCode**
2. Go to the **"Run and Debug"** tab on the left bar
3. Create or edit the file `.vscode/launch.json` with configurations for each component
4. For `RMIBarrel.java`, make sure to pass **one integer argument** representing the barrel ID (e.g., `1`, `2`, `3`...)

Example configuration snippet:
{
  "type": "java",
  "name": "Run Barrel 1",
  "request": "launch",
  "mainClass": "src.Barrels.RMIBarrel",
  "args": ["1"]
}

## 🖥️ Running in IntelliJ IDEA

### Running Multiple Barrels Simultaneously:

1. **Go to Run → Edit Configurations**
2. **Create multiple Application configurations:**
   - Name: `Barrel 1`, Main class: `src.Barrels.RMIBarrel`, Program arguments: `1`
   - Name: `Barrel 2`, Main class: `src.Barrels.RMIBarrel`, Program arguments: `2`
3. **Enable parallel execution:**
   - Check **"Allow parallel run"** for each configuration
   - Or create a **Compound Configuration** to run all barrels at once

### Execution Order in IntelliJ:
1. Run `UrlQueue` first
2. Run `RMIGateway`
3. Run multiple `RMIBarrel` instances (with different IDs)
4. Run `RMIClient`

## ⚙️ Configuration.java

All global settings (ports, IPs, RMI registry, number of barrels, downloaders, etc.) are defined here.

**Important configuration notes:**

- If **Gateway** and **Client** are on the same machine → set `GATEWAY_IP = "localhost"`
- Otherwise, set it to the **private IP** of the gateway host (they must be on the same network)
- Modify the constants according to your needs:
  - `NUM_BARRELS`
  - `NUM_DOWNLOADERS`
  - `MULTICAST_PORT` and `MULTICAST_ADDRESS`
  - `RECEIVE_PORT`
  - `NUM_MISSES` (tolerance for retries in case of failure)

## 🔧 Component Details

### RMIBarrel.java
- Runs with a single integer argument representing the barrel ID
- Validates ID range (1 to NUM_BARRELS)
- Maintains inverted indexes split alphabetically (A-M and N-Z)
- Stores data persistently in text files
- Sends status updates via multicast

### Downloader Component
- Connects to UrlQueue via TCP to get URLs for processing
- Uses JSoup for web scraping and content extraction
- Sends processed data to barrels via multicast
- Follows links to discover new URLs

### UrlQueue System
- TCP server that maintains a queue of URLs to be processed
- Downloaders connect to get URLs for indexing
- Supports URL submission from clients

## 🌐 External API Integration

### 🟥 YouTube API
Implemented in `src/WebServer/YoutubeAPI/YoutubeAPI.java`.  
It uses a **Google Cloud API key** stored as an environment variable (`GOOGLE_API_KEY`).  
You must enable **YouTube Data API v3** in your Google Cloud Console and set:

bash
export GOOGLE_API_KEY="your_api_key_here"


## This project was developed exclusively for academic purposes under the course Sistemas Distribuídos (FCTUC), 2024/2025.

🔗 References
Java RMI Documentation

JSoup Web Scraping Library

YouTube Data API v3

HackerNews API
