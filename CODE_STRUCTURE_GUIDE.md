# Code Structure and Algorithm Guide

## Table of Contents
1. [Overall System Structure](#overall-system-structure)
2. [How the Application Works](#how-the-application-works)
3. [Core Components](#core-components)
4. [Algorithm Implementations](#algorithm-implementations)
5. [AI Components](#ai-components)
6. [Data Flow](#data-flow)

---

## Overall System Structure

### Project Organization

```
src/main/java/com/example/os/
├── HelloApplication.java          # Main entry point, starts JavaFX app
├── SchedulerController.java        # Handles UI and coordinates everything
├── model/                          # Data structures
│   ├── Process.java               # Represents a single process
│   ├── ScheduleResult.java        # Contains scheduling results
│   ├── GanttEntry.java            # Represents one bar in Gantt chart
│   └── ComparisonResult.java      # Stores comparison data
├── scheduler/                      # All scheduling algorithms
│   ├── Scheduler.java             # Base class for all schedulers
│   ├── FCFSScheduler.java         # First Come First Served
│   ├── SJFScheduler.java          # Shortest Job First
│   ├── RoundRobinScheduler.java    # Round Robin
│   ├── PriorityScheduler.java     # Priority Scheduling
│   └── IntelligentAIScheduler.java # AI-powered scheduler
└── ai/                            # AI components
    ├── AIPredictor.java           # Predicts burst times
    └── SchedulerRecommender.java   # Recommends best algorithm
```

### Key Design Pattern

- **MVC Pattern**: Model (data), View (UI), Controller (logic)
- **Strategy Pattern**: Each scheduler implements same interface
- **Template Method**: Base Scheduler class defines common structure

---

## How the Application Works

### Step-by-Step Flow

1. **Application Starts**
   - HelloApplication.java launches JavaFX window
   - Loads scheduler-view.fxml (UI layout)
   - Creates SchedulerController instance

2. **User Adds Processes**
   - User clicks "Add Process" or "Random Generate"
   - Processes stored in processList (ObservableList)
   - Table displays process details

3. **User Selects Algorithm**
   - Chooses from dropdown: FCFS, SJF, Round Robin, Priority, or AI Scheduler
   - For Round Robin, can set time quantum

4. **User Clicks "Start Single" or "Compare All"**
   - Controller creates scheduler instance
   - Passes process list to scheduler
   - Scheduler executes algorithm
   - Returns ScheduleResult with metrics

5. **Results Displayed**
   - Process table updated with waiting/turnaround times
   - Gantt chart drawn showing execution timeline
   - Average metrics displayed
   - For comparison: table shows all algorithms side-by-side

### Main Classes and Their Roles

**HelloApplication.java**
- Entry point of application
- Sets up JavaFX window
- Loads FXML file
- Handles window close events

**SchedulerController.java**
- Manages all user interactions
- Handles button clicks
- Creates scheduler instances
- Updates UI with results
- Manages process list

**Scheduler.java (Base Class)**
- Abstract class all schedulers extend
- Defines schedule() method
- Provides calculateMetrics() helper
- Ensures consistent interface

---

## Core Components

### Process Model

**Process.java** - Represents one process

```java
Key Fields:
- id: Unique identifier (1, 2, 3...)
- arrivalTime: When process enters system
- burstTime: How long process needs CPU
- priority: Process priority (lower = higher priority)
- waitingTime: Time spent waiting (calculated)
- turnaroundTime: Total time from arrival to completion (calculated)
- completionTime: When process finishes (calculated)
- remainingTime: How much CPU time still needed (for preemptive)
- startTime: When process first starts executing (calculated)
```

**Important Methods:**
- `copy()`: Creates duplicate process for scheduling (doesn't modify original)

### ScheduleResult

**ScheduleResult.java** - Contains algorithm results

```java
Fields:
- processes: List of processes with calculated metrics
- ganttChart: List of execution segments for visualization
- avgWaitingTime: Average waiting time across all processes
- avgTurnaroundTime: Average turnaround time across all processes
```

### GanttEntry

**GanttEntry.java** - One bar in Gantt chart

```java
Fields:
- processId: Which process executed
- startTime: When execution started
- endTime: When execution ended
```

---

## Algorithm Implementations

### Common Pattern

All schedulers follow this pattern:

1. Create copies of processes (don't modify originals)
2. Sort processes by arrival time
3. Initialize ready queue and current time
4. Loop until all processes complete:
   - Add arrived processes to ready queue
   - Select next process to execute
   - Execute process (fully or partially)
   - Calculate metrics
   - Update Gantt chart
5. Calculate averages
6. Return ScheduleResult

### 1. FCFS (First Come First Served)

**File:** FCFSScheduler.java

**How it works:**
- Simplest algorithm
- Processes execute in order they arrive
- Non-preemptive (once started, runs to completion)

**Code Logic:**
```
1. Sort processes by arrival time
2. For each process in order:
   a. If CPU is idle, wait until process arrives
   b. Set startTime = currentTime
   c. Calculate waitingTime = startTime - arrivalTime
   d. Execute process completely
   e. Calculate completionTime = startTime + burstTime
   f. Calculate turnaroundTime = completionTime - arrivalTime
   g. Move to next process
```

**Key Points:**
- No preemption
- Simple to implement
- Can cause convoy effect (short jobs wait behind long ones)

**Example:**
```
Process 1: Arrives at 0, Burst 5 → Starts at 0, Completes at 5
Process 2: Arrives at 1, Burst 3 → Starts at 5, Completes at 8
Process 3: Arrives at 2, Burst 8 → Starts at 8, Completes at 16
```

### 2. SJF (Shortest Job First)

**File:** SJFScheduler.java

**How it works:**
- Always picks shortest job from ready queue
- Non-preemptive
- Minimizes average waiting time

**Code Logic:**
```
1. Sort processes by arrival time, then by burst time
2. Initialize ready queue and current time
3. While processes remain:
   a. Add all arrived processes to ready queue
   b. If ready queue empty, advance time to next arrival
   c. Sort ready queue by burst time (shortest first)
   d. Select and execute shortest process
   e. Calculate metrics
   f. Repeat
```

**Key Points:**
- Requires knowing burst times
- Optimal for minimizing waiting time
- Can starve long processes

**Example:**
```
Time 0: P1 arrives (burst 5), P2 arrives (burst 3)
Ready queue: [P2(3), P1(5)]
Execute P2 (shorter) → Completes at 3
Execute P1 → Completes at 8
```

### 3. Round Robin

**File:** RoundRobinScheduler.java

**How it works:**
- Preemptive algorithm
- Each process gets time quantum
- If not done, goes back to end of queue

**Code Logic:**
```
1. Sort processes by arrival time
2. Create FIFO ready queue
3. While processes remain:
   a. Add arrived processes to ready queue
   b. If ready queue empty, advance time
   c. Dequeue first process
   d. Execute for min(quantum, remainingTime)
   e. If process not done, add back to queue
   f. If done, calculate final metrics
   g. Check for new arrivals after execution
   h. Repeat
```

**Key Points:**
- Preemptive (can interrupt)
- Fair (everyone gets turns)
- Time quantum affects performance
- Higher context switching overhead

**Example (Quantum = 2):**
```
Time 0: P1 executes for 2 units (remaining 3)
Time 2: P2 arrives, P1 goes to back, P2 executes for 2
Time 4: P1 executes for 2 more (remaining 1)
Time 6: P1 executes for 1 (completes)
```

### 4. Priority Scheduling

**File:** PriorityScheduler.java

**How it works:**
- Executes highest priority process first
- Lower priority number = higher priority
- Non-preemptive

**Code Logic:**
```
1. Sort processes by arrival time, then by priority
2. Initialize ready queue
3. While processes remain:
   a. Add arrived processes to ready queue
   b. If ready queue empty, advance time
   c. Sort ready queue by priority (lowest number first)
   d. Execute highest priority process completely
   e. Calculate metrics
   f. Repeat
```

**Key Points:**
- Priority-based selection
- Can starve low priority processes
- Useful for real-time systems

**Example:**
```
P1: Priority 3, P2: Priority 1, P3: Priority 2
Ready queue sorted: [P2(1), P3(2), P1(3)]
Execute P2 first (highest priority)
```

### 5. Intelligent AI Scheduler

**File:** IntelligentAIScheduler.java

**How it works:**
- Uses AI to predict burst times
- Multi-factor scoring system
- Adaptive preemption
- Learns from execution history

**Code Logic:**
```
1. Store original burst times (needed for accurate metrics)
2. Use AI predictor to optimize burst times
3. Sort by arrival time
4. While processes remain:
   a. Add arrived processes to ready queue
   b. Calculate score for each ready process:
      - Priority score (25%)
      - Predicted burst score (30%)
      - Waiting time score (25%)
      - Urgency score (20%)
   c. Select process with highest score
   d. Check if should preempt (if better process available)
   e. Execute for adaptive quantum or until completion
   f. Check for new arrivals after execution
   g. If completed, calculate metrics using original burst time
   h. Update AI predictions
5. Update original processes with results
6. Learn from execution (adjust learning rate)
```

**Scoring Formula:**
```
Total Score = (Priority × 0.25) + (Burst × 0.30) + (Waiting × 0.25) + (Urgency × 0.20)

Where:
- Priority = 1 / (1 + priority_number)
- Burst = 1 / (1 + predicted_burst_time)
- Waiting = log(1 + waiting_time) / 10
- Urgency = calculated based on queue state and process characteristics
```

**Key Points:**
- Combines multiple factors intelligently
- Adapts to process patterns
- Uses original burst time for accurate metrics
- Learns and improves over time

**Preemption Logic:**
- Preempts if better process available (20% better score)
- Only if current process has run for at least 2 time units
- Uses adaptive quantum based on process characteristics

---

## AI Components

### AIPredictor

**File:** AIPredictor.java

**Purpose:**
- Predicts future burst times based on history
- Uses adaptive exponential averaging
- Detects patterns and trends

**How it works:**

**1. Adaptive Exponential Averaging:**
```
Prediction = α × Actual + (1 - α) × Previous_Prediction

Where α (alpha) adapts based on process variance:
- Stable process (low variance): α = 0.1-0.2 (trust history)
- Variable process (high variance): α = 0.8-0.9 (trust recent)
- Moderate variance: α = 0.3-0.7 (balanced)
```

**2. Pattern Recognition:**
- Maintains history of last 10 executions
- Calculates variance to determine stability
- Detects trends (increasing/decreasing burst times)
- Applies trend adjustments to predictions

**Key Methods:**
- `updatePrediction()`: Updates prediction after execution
- `getPredictedBurstTime()`: Returns predicted burst time
- `optimizeProcessList()`: Replaces actual burst times with predictions

### SchedulerRecommender

**File:** SchedulerRecommender.java

**Purpose:**
- Compares results from all algorithms
- Recommends best algorithm
- Calculates performance scores

**How it works:**
```
Score = 0.5 × (1 / (1 + avg_waiting)) + 0.5 × (1 / (1 + avg_turnaround))

Higher score = Better performance
```

**Methods:**
- `getBestResult()`: Finds algorithm with highest score
- `recommendBest()`: Returns name of best algorithm

---

## Data Flow

### Single Algorithm Execution

```
User Input
    ↓
SchedulerController.handleStart()
    ↓
Creates scheduler instance
    ↓
Scheduler.schedule(processList)
    ↓
Algorithm executes:
  - Creates process copies
  - Schedules processes
  - Calculates metrics
  - Creates Gantt chart
    ↓
Returns ScheduleResult
    ↓
SchedulerController.updateUI()
    ↓
UI Updates:
  - Process table refreshed
  - Gantt chart drawn
  - Metrics displayed
```

### Comparison Execution

```
User Clicks "Compare All"
    ↓
SchedulerController.runComparison()
    ↓
For each algorithm:
  - Create process copies
  - Create scheduler instance
  - Execute algorithm
  - Store results
    ↓
Create ComparisonResult for each
    ↓
Calculate scores
    ↓
Find best algorithm
    ↓
Display comparison table
    ↓
Show ML recommendation
```

### Process Lifecycle

```
1. Process Created
   - User adds process or generates random
   - Stored in processList

2. Process Scheduled
   - Copy created (original preserved)
   - Added to ready queue when arrives
   - Selected by algorithm
   - Executed (fully or partially)

3. Metrics Calculated
   - Waiting time = start time - arrival time
   - Turnaround time = completion time - arrival time
   - Completion time = start time + total execution time

4. Results Displayed
   - Original process updated with metrics
   - Table refreshed
   - Gantt chart updated
```

---

## Key Concepts Explained

### Preemptive vs Non-Preemptive

**Non-Preemptive (FCFS, SJF, Priority):**
- Once process starts, it runs to completion
- No interruption
- Simpler to implement
- Lower context switching overhead

**Preemptive (Round Robin, AI Scheduler):**
- Process can be interrupted
- Executes in time slices
- More fair
- Higher overhead due to context switching

### Ready Queue

- List of processes waiting to execute
- Processes added when they arrive
- Algorithm selects from ready queue
- Different algorithms use different selection criteria

### Time Quantum (Round Robin)

- Fixed time slice given to each process
- If process doesn't complete, it's preempted
- Goes back to end of queue
- Typical values: 2-10 time units

### Metrics Calculation

**Waiting Time:**
```
waitingTime = startTime - arrivalTime
```
Time process spends waiting before first execution

**Turnaround Time:**
```
turnaroundTime = completionTime - arrivalTime
```
Total time from arrival to completion

**Completion Time:**
```
completionTime = startTime + burstTime (non-preemptive)
completionTime = last_execution_end (preemptive)
```
When process finishes execution

### Safety Checks

All algorithms include:
- `Math.max(0, value)` to prevent negative times
- Empty list checks
- Process completion verification
- Original burst time preservation (AI Scheduler)

---

## Common Issues and Solutions

### Issue: Negative Values

**Cause:** Calculation errors or edge cases

**Solution:** All schedulers use `Math.max(0, value)` to ensure non-negative

### Issue: Processes Not Completing

**Cause:** Missing new arrival checks in loops

**Solution:** Check for new arrivals after each execution (especially in preemptive algorithms)

### Issue: Wrong Metrics in AI Scheduler

**Cause:** Using optimized burst time instead of original

**Solution:** Store original burst times and use them for metric calculations

### Issue: Empty Process List

**Cause:** No processes added or list cleared

**Solution:** Check if list is empty before scheduling, show alert to user

---

## Testing Your Understanding

### Questions to Check Understanding

1. Why do we create copies of processes before scheduling?
2. What's the difference between waiting time and turnaround time?
3. Why does Round Robin need to check for new arrivals after execution?
4. How does AI Scheduler decide which process to execute next?
5. Why does AI Scheduler store original burst times?

### Answers

1. To avoid modifying original processes, allowing comparison and reset
2. Waiting time is time before first execution, turnaround time is total time from arrival to completion
3. Because execution takes time, new processes may arrive during execution
4. Uses multi-factor scoring combining priority, predicted burst, waiting time, and urgency
5. Because it optimizes burst times for scheduling, but needs originals for accurate metric calculations

---

## Summary

### System Architecture
- MVC pattern separates concerns
- Strategy pattern allows easy algorithm swapping
- Base Scheduler class ensures consistency

### Algorithm Selection
- FCFS: Simple, first-come basis
- SJF: Optimal waiting time, requires burst time knowledge
- Round Robin: Fair, preemptive, good for time-sharing
- Priority: Priority-based, useful for real-time systems
- AI Scheduler: Intelligent, adaptive, learns from history

### Key Design Decisions
- Process copying preserves originals
- Safety checks prevent negative values
- Original burst time preservation ensures accuracy
- Modular design allows easy extension

This structure makes the code maintainable, testable, and easy to understand.

