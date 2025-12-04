# 🖥️ OS Process Scheduler

## 📌 Overview
The **OS Process Scheduler** is a simulation-based project demonstrating how CPU scheduling algorithms manage processes within an operating system. It allows users to compare popular scheduling strategies including **FCFS**, **SJF**, **Priority Scheduling**, and **Round Robin**. The scheduler computes performance metrics and generates a **Gantt Chart** for visualization.

## ✨ Features
- Supports multiple CPU scheduling techniques
- Dynamic input for process attributes
- Computes:
  - Average Waiting Time (AWT)
  - Average Turnaround Time (ATAT)
  - Response Time
  - Context Switching
- Performance comparison of algorithms
- Gantt chart visual output

## ⚙️ Algorithms Implemented
| Algorithm | Description |
|-----------|-------------|
| FCFS | Non-preemptive, arrival-based execution |
| SJF | Executes process with shortest burst time |
| Priority | Execution based on process priority |
| Round Robin | Time quantum based scheduling |

## 📂 Project Structure
```
/src         -> Source code files
/input       -> Sample test cases
/output      -> Output results and charts
```

## 🧪 Input Format
```
Process ID | Arrival Time | Burst Time | Priority
```

## 📊 Example Output
```
Selected Algorithm: SJF
Average Waiting Time: 5.23ms
Average Turnaround Time: 9.45ms
Gantt Chart: P2 | P1 | P4 | P3
```

## 🚀 How to Run
```bash
git clone https://github.com/your-repo/OS-Process-Scheduler.git
cd OS-Process-Scheduler
javac Main.java
java Main
```

## 🛠 Tech Stack
- Language: C / C++ / Java / Python
- Concepts: CPU Scheduling, OS Process Management

## 🤝 Contributions
Contributions welcome! Feel free to submit pull requests or issues.

## 📜 License
MIT License
