# Developer Guide for FinanceIt

* Table of Contents
{:toc}
# Design

## Overview of Architecture
__Architecture Diagram__

![](uml_images/images_updated/Overall.png)

There are 5 distinct features that exists within the FinanceIt application, all of which are accessed via the main menu 
interface facilitated in FinanceIt.java.

The design of the software can be split into 5 distinct components:
* Logic Manager component
* Logic component
* Input Manager component
* Data component
* Storage component

## Logic Manager Component

![](uml_images/images_updated/Handler_arch.png)

__Description__

The Logic Manager component serves as the bridge between user interface and program operations.
It includes 4 classes: 
* ```ManualTracker```
* ```EntryTracker```
* ```RecurringTracker```
* ```GoalTracker```
* ```FinanceTools```

__API__
* ```ManualTracker```, `RecurringTracker` and ```EntryTracker``` maintains an instance of a ```DataList``` (```LedgerList``` and ```EntryList```) in ```Model``` respectively, 
 and provides an interface for the user can append, remove or perform other ```Data``` operations with the contents of the ```Datalist```.
* ```GoalTracker``` maintains a list of income or expense ```Goals``` to track against entries in the ```EntryList```, 
and provides an interface for the user to append or remove ```Goals```.
* ```Finance Tools``` class provides an interface for users to utilize an array of 
finance calculator tools within it.
* All ```LogicManager``` classes use the ```InputManager``` component to process user input, then use ```Logic``` component
to perform the operation associated with the user input.

## Logic Component

![](uml_images/images_updated/Logic_arch.png)

__Description__

The Logic Component abstracts logic operations that are dependent on `params` passed via a `CommandPacket` instance.

__API__

* `CommandHandler` classes are used in `LogicManager` classes to handle parts of the operations which are dependent on
the `params` supplied.
* If `CommandHandler` classes recognises a `param` from the `CommandPacket` instance, it performs a sub-operation
associated with the `param`. For instance, `/date` will cause `CreateLedgerCommand` instance to set the date of the
new instantiated ledger.  
* `ParamChecker` verifies correctness of `params` before it is processed further by a `CommandHandler` class.




## Input Manager Component

![](uml_images/images_updated/InputManager.png)

__Description__

The Input Manager consists of the ```UiManager``` class, and the ```Parser``` sub-component.

__API__

* ```handleInput()``` from the ```UiManager``` class is called from ```Handler``` classes to 
retrieve the raw string input from the user.
* ```Parser``` subcomponent classes are responsible for parsing raw String input from the user
and produce an equivalent ```CommandPacket``` instance.
* ```Handler``` classes will use the ```CommandPacket``` instance to call the corresponding
```Command``` classes or perform the next operation.

## Model Component

![](uml_images/images_updated/Data_arch.png)

__Description__

Represents data and data list in the program, whereby program operations specified
by user input can be performed upon.

__API__

* ```EntryTracker``` and ```ManualTracker``` classes can interact with ```LedgerList``` and ```EntryList```
instances to perform add, remove or edit operations on the ```Ledgers``` or ```Entry``` instances in it.
* ```Storage``` component interact with ```DataList``` classes for save and load operations.
    * For save, ```Storage``` component uses the ```EntryTracker``` and ```ManualTracker``` instances in the program
    at the point of save to write to a series of text files that persists after the program closes.
    * For load, ```Storage``` component writes data from the text files to ```EntryTracker``` and ```ManualTracker``` respectively.
     

## Storage Component

![](uml_images/saveManager/SaveManagerClassSimpilfied.png)

__Description__

Storage component performs storage of data from Goal Tracker, Manual Tracker and Recurring Tracker. It loads
the data upon entry into the program and performs auto save upon exiting the program. Save Manager also added
a feature that allow multiple copies of backup data to be saved and loaded.

__API__

* ```manualTrackerSaver```, ```goalTrackerSaver``` and ```autoTrackerSaver``` inherits some common methods
from ```saveHandler```. The saver classes are primarily used by ```saveManager``` for file input output operations.



# Implementation
## Module-level Implementation
### Logic Manager Component
![](uml_images/images_updated/Handler.png)

**Execution** <br />
1. Logic Managers are implemented with a common method: ```execute()```, which utilizes a `while loop`
to maintain a cycle of 2 processes: User input processing and Command handling.
**User Input Processing** <br />
1. Logic Managers depend on InputManager module to read user input, parse user input and produce a 
meaningful ```CommandPacket``` instance.
1. The ```CommandPacket``` instance can then be used by the next step of the cycle.
**Command Handling** <br />
1. Each Logic Manager will have several methods that are dedicated to handle a single operation. They can
typically be identified by a specific naming convention: `"handle.....()"`.
1. These methods use ```CommandHandler``` classes to perform `param` dependent operations, which involves evaluation
of `paramMap` in the provided `CommandPacket` instance to decide the operation to perform, be it on `Data` or `DataList`.
**Error Reporting** <br />
1. While error handling from `param` parsing is handled by `ParamChecker` singleton class, there is a need
to identify from the execution methods at Logic Managers, whether an exception has been thrown. 
1. This is handled by a `try-catch block` within the  `"handle.....()"` methods, whereby an exception caught
will result in an error message printed. The error message will not be specific to the exact error; rather it 
generally indicates whether an operation has failed.

**Example** <br />
* Execute Method

```
    public static void execute() {
        endTracker = false;
        UiManager.printWithStatusIcon(Common.PrintType.SYS_MSG, "Welcome to Manual Tracker!");
        while (!endTracker) {
            endTracker = false;
            handleMainMenu();
        }
    }
```

* Operation Methods

```
    static void handleDeleteLedger() {
        Ledger deletedLedger;
        RetrieveLedgerHandler retrieveLedgerHandler = RetrieveLedgerHandler.getInstance();
        try {
            // RetrieveledgerHandler instance retrieves the corresponding ledger instance
            // from the ledgerList instance.
            retrieveLedgerHandler.handlePacket(packet, ledgerList);
            deletedLedger = (Ledger) ledgerList.getItemAtCurrIndex();

            // Deletion of ledger.
            ledgerList.removeItemAtCurrIndex();
            UiManager.printWithStatusIcon(Common.PrintType.SYS_MSG,
                String.format("%s deleted!", deletedLedger.getName()));
        } catch (InsufficientParamsException | ItemNotFoundException exception) {
            UiManager.printWithStatusIcon(Common.PrintType.ERROR_MESSAGE,
                exception.getMessage());
        } finally {
            if (!retrieveLedgerHandler.getHasParsedAllRequiredParams()) {
                UiManager.printWithStatusIcon(Common.PrintType.ERROR_MESSAGE,
                    "Input failed due to param error.");
            }
        }
    }
```

### Logic Component
![](uml_images/images_updated/Logic.png)
**ParamChecker** <br />
1. Contains a set of public static methods which are used to verify the correctness of `param` in the 
```CommandPacket``` instance.
1. If there is nothing wrong with the ```param```, the method will typically return the `param` supplied without modification.
1. If the ```param``` fails to pass the tests administered within the method, the following procedures will execute:
    1. Log to log file a corresponding error message with ```WARNING``` level
    1. Print to console, any applicable error messages.
    1. Throw a common exception: `ParseFailParamException` 
        1. The implication is that the range of exceptions that would have been caught in other
        parts of the software with regards to param handling, is now consolidated within a single class in the program.
        The class that uses ParamChecker is only concerned with whether the ```param``` is valid or not.
1. Example:
    * The following method checks validity of dates supplied from user input.
    * It is used by `createledgerHandler` class. 

```
    public LocalDate checkAndReturnDate(String paramType)
        throws ParseFailParamException {
        LocalDate date = null;
        boolean parseSuccess = false;

        clearErrorMessage();

        LoggerCentre.loggerParamChecker.info("Checking date...");
        try {
            String rawDate = packet.getParam(paramType);
            if (rawDate.trim().length() == 0) {
                throw new EmptyParamException(paramType);
            }
            date = DateTimeParser.parseLocalDate(rawDate);
            parseSuccess = true;
        } catch (DateTimeException exception) {
            LoggerCentre.loggerParamChecker.warning(
                String.format("Date parsed but not valid... Err: %s", exception.getMessage()));

            errorMessage = getErrorMessageDateDateTimeException();
        } catch (InvalidParameterException exception) {
            LoggerCentre.loggerParamChecker.warning(
                String.format("Date input cannot be parsed... Err: %s", exception.getMessage()));

            errorMessage = getErrorMessageDateInvalidFormat();
        } catch (EmptyParamException exception) {
            LoggerCentre.loggerParamChecker.warning(
                String.format("No date input supplied... Err: %s", exception.getMessage()));

            errorMessage = UiManager.getStringPrintWithStatusIcon(Common.PrintType.ERROR_MESSAGE,
                exception.getMessage(),
                "Enter \"commands\" to check format!");
        } finally {
            printErrorMessage();
        }
        if (!parseSuccess) {
            throw new ParseFailParamException(paramType);
        }
        return date;
    }
```

**ParamHandler** <br />
1. After parsing from user input to produce a ```commandPacket``` instance, the instance needs to be handled by a particular ```ParamHandler``` children class,
which processes the ```commandPacket``` attributes to perform a specific function. 

1. Handling of params via```handleParams(packet)```:
    1. Initialize the state of the handler 
        1. Children class of ```ParamHandler``` call ```setRequiredParams()``` to set required Params that need to be parsed successfully to constitute an overall successful parse.
        1. Resetting String arrays in the following ```param``` arrays:
            * ```missingRequiredParams```
            * ```paramsSuccessfullyParsed```
        1. Set the ```CommandPacket``` instance in ```ParamChecker``` by calling ```ParamChecker.setPacket(packet)```.
    1. Call `handleParams()`
        1. For every```paramType``` in the ```CommandPacket``` instance, execute ```handleSingleParam(packet)``` method. 
        1. ```handleSingleParam(packet)``` is an abstract method, and it is implemented by children classes of ```ParamHandler``` depending on the needs and requirements of that particular class.
        1. If the `param` fail to be parsed due to input error, an exception from `ParamChecker`: `ParseFailParamsException` will be caught.
        The error message from `ParamChecker` will be printed.
        1. Else if the `param` parses successfully, it will be added to ```paramsSuccessfullyParsed```
    1. Check if the parse was successful. The condition below that define a successful parse is:
        1. All ```param``` in ```createledgerHandler.requiredParams``` string array are parsed with no exceptions thrown.
        That is, all `param` in ```createledgerHandler.requiredParams``` is also in ```paramsSuccessfullyParsed```.
    1. If parse is successful, the process ends gracefully. Else, throw ```InsufficientParamsException()```.

**CommandHandler** <br />
1. Extends `ParamHandler` class. Implements ```handleSingleParams()``` fully, depending on the interactions
between the operation and the `param` that it accepts. 
1. Typically used within Logic Managers to handle processing of `CommandPacket` instances to decide sub-operations
to perform to achieve full operation specified by the user. 
1. Example:`handleDeleteLedger()`
    1. Uses `retrieveledgerHandler` to interpret the `ledger` instance to deleted, as specified by the user
    1. Retrieves the `ledger` instance and performs delete within the method.   


### Input Manager Component
![](uml_images/images_updated/InputManager.png)


### Model Component
![](uml_images/images_updated/Data.png)

### Storage Component
Please refer to the section below:
[Storage Utility](#storage utility)

## Feature-level Implementation
### Main Menu
- Loading up user data
- Access to various features
- Saving outstanding user data to respective save files

### Manual Tracker & Entry Tracker
**Overview** <br />
__Ledgers and Entries__

In this feature, we represent the transactions incurred by the users as ```Entry``` instances.
Instances of ```Entry``` class are categorised by the date of origin, which is represented by
```Ledger``` instances.

```Entry``` instances are characterized by the following: 
* Time of transaction
* Type of transaction: Income/ Expense 
* Amount in transaction
* Category of spending/ expenditure
* Description

```Ledger``` instances are characterized by the following: 
* Time of transaction
* Collection of ```Entry```instances

**Manual Tracker** <br />

The Manual Tracker is a feature that allows users to manage Ledgers with create, delete
and open operations. Ledgers is a class that maintains a list of transactions that are 
recorded for a given date. 

The Entry Tracker is fundamentally similar to the Manual Tracker, except it manages ```Entry``` instances
instead of ```Ledger```. Entry Tracker is initialized when a ```Ledger``` instance is "opened", whereby 
the Entry Tracker facilitate the manipulation of the collection of ```Entry``` instances that are associated with
that particular ```Ledger``` instance.

For the sake of brevity, this section will focus on the discussion of the Manual Tracker. Section [2.2.2.3] (#2.2.2.3) will describe
the edit operation of the Entry Tracker, which is sufficiently unique to Manual Tracker operations to merit detailed discussion.

The Manual Tracker is capable of executing the following states of operation:

|States| Operations | 
|--------|----------|
|```MAIN_MENU```|Go to main menu for users to choose the available operations
|```CREATE_LEDGER```|Create a ledger specified by date, and append it to ```ledgerList```.
|```DELETE_LEDGER```|Delete an existing ledger, referenced by date or index.
|```OPEN_LEDGER```|Go to subroutine "Entry Tracker" for the entries recorded  under the specified ledger.

**Architecture in Context** <br />

**Logic Manager and Parser** <br />

![](uml_images/images_updated/Handler_Parser.png)

|Class| Function |
|--------|----------|
|```InputParser```| Breaks input string by user into ```commandString``` and a sequence of ```paramTypes```-```param``` pairs. <br><br> The latter subsequence of the string is passed into ParamParser for further processing. <br><br> Information obtained from input parsing will be used to populate an instantiated ```CommandPacket``` instance, which will then be passed to the entity that called the parsing function.
|```ParamParser```| Process the sequence of ```paramTypes```-```param``` pairs and populate the ```paramMap``` in the instantiated ```CommandPacket``` instance.
|```ManualTracker```| [Refer to section above](#LogicManagerAndHandler).
|```EntryTracker```| Omitted for brevity.

**Logic Manager and Data** <br />

![](uml_images/images_updated/Handler_Data.png)

|Class| Function |
|--------|--------|
|```ManualTracker```| [Refer to section above](#LogicManagerAndHandler).
|```EntryTracker```| Omitted for brevity.
|```EntryList```| Omitted for brevity.
|```Entry```| Omitted for brevity.
|```LedgerList```| Extends ItemList. Refer to Ledgers and Entries section for class behavior.
|```Ledger```| Extends DateTimeItem. Refer to Ledgers and Entries section for class behavior.
|```ItemList```| Class with defined list behavior specified with helper methods such as retrieval, checking of Duplicates and deletion.
|```DateTimeItem```| Abstract class that extends ```Item``` class; instances will have ```LocalDate``` or ```LocalTime``` attributes and corresponding helper methods.
|```Item```| Abstract class to define behavior of entities that need are stored in ```ItemList``` instances.

**Handler and Logic** <br />

![](uml_images/images_updated/Commands_Logic.png)

|Class| Function |
|--------|----------|
|```retrieveledgerHandler```| Process ```paramTypes```-```param``` pairs from the ```CommandPacket``` instance to identify specified ```Ledger``` instance, then retrieves the instance from the existing ```LedgerList```.
|```createledgerHandler```| Process ```paramTypes```-```param``` pairs from the ```CommandPacket``` instance to identify specified ```Ledger``` instance to be created, then creates the instance and append to existing ```LedgerList```.
|```retrieveEntryHandler```| Omitted and left as exercise for reader. : ^ )
|```createentryHandler```| Omitted for brevity.
|```editEntryHandler```| Omitted for brevity.
|```ParamChecker```| Class contains a collection of methods that verify the correctness of the ```param``` supplied. <br><br> For instance, ```ParamChecker.checkAndReturnIndex``` checks if the index provided is out of bounds relative to the specified list, and throws the relevant exception if the input index is invalid. 
|```ParamHandler```| Abstract class that outlines the general param handling behavior of ```commands``` instances and other classes that need to handle ```params``` in its operation.  

**Logic Manager and Handler** <br />

![](uml_images/images_updated/Handler_Commands.png)

|Class| Function |
|--------|----------|
|```retrieveledgerHandler```| [Refer to section above](#handlerAndLogic).
|```createledgerHandler```| [Refer to section above](#handlerAndLogic).
|```retrieveEntryHandler```| Omitted for brevity.
|```createentryHandler```| Omitted for brevity.
|```editEntryHandler```| Omitted for brevity.
|```ManualTracker```| Implements Manual Tracker. Contains handler methods that implements a particular operation capable by the Manual Tracker. <br><br> These methods use the above ```command``` instances for param handling operations from user input.
|```EntryTracker```| Omitted for brevity.


**Functions with Sequence Diagrams** <br />

**Creation of Ledger** <br />
1. At ```ManualTracker.handleMainMenu()```, the user's input is registered via ```java.util.Scanner``` instance.
1. Input is parsed by ```InputParser.parseInput()```, and ```ManualTracker.packet``` is set to the returned ```CommandPacket``` instance.
1. The ```commandString``` of the ```CommandPacket``` instance is evaluated, and the corresponding handle method() is executed.<br>
In this case, ```handleCreateLedger()``` will be called.
1. At ```handleCreateLedger()```, the following processes will be executed:
    1. A new instance of ```createledgerHandler``` is created. The input String array will be passed into 
    ```createledgerHandler.setRequiredParams()``` to set required params for a successful parse.
    1. A new instance of ```Ledger``` will be instantiated and set to ```createledgerHandler.currLedger```.
    1. ```createledgerHandler.handlePacket(packet)``` is called to handle params in the packet.
        1. Refer to the [section on Param Handling](#paramHandling) for more details pertaining to general param handling. 
        1. For ```createledgerHandler```, the ```handleSingleParam``` abstract method will be implemented as follows:
        
|ParamType|ParamType String| Expected Param | Operation | Verification method |
|---------|----------------|----------------|-----------|---------------------|
|```PARAM.DATE```|"/date"|Various format of date in string, eg. "2020-03-02"| Call ```currLedger.setDate()``` to set date for the ```Ledger``` instance. | ```ParamChecker.checkAndReturnDate(packet)```|
1. From ```ManualTracker```, the configured ```Ledger``` instance will be retrieved from the ```createledgerHandler``` instance
and added into the ```LedgerList``` instance at ```ManualTracker.ledgerList```.
  
![](uml_images/images_updated/manualTrackerCreateLedgerSeqDiagram.png)


**Deletion of Ledger** <br />
The deletion of a specified ledger is performed in two phases: Ledger Retrieval and Ledger Delete.
1. __Phase 0: Instruction retrieval__ 
    1. At ```ManualTracker.handleMainMenu()```, the user's input is registered via ```java.util.Scanner``` instance.
    1. Input is parsed by ```InputParser.parseInput()```, and ```ManualTracker.packet``` is set to the returned ```CommandPacket``` instance.
    1. The ```commandString``` of the ```CommandPacket``` instance is evaluated, and the corresponding handle method() is executed.<br>
    In this case, ```handleDeleteLedger()``` will be called.
1. __Phase 1: Ledger retrieval__
    1. At ```handleDeleteLedger()```, the following processes will be executed:
        1. A new instance of ```retrieveledgerHandler``` is created. The input String array will be passed into 
        ```createledgerHandler.setRequiredParams()``` to set required params for a successful parse.
        1. ```deleteledgerHandler.handlePacket(packet)``` is called to handle params in the packet.
            1. Refer to the section on [Param Handling](#paramHandling) for more details pertaining to general param handling. 
            1. For ```createledgerHandler```, the ```handleSingleParam``` abstract method will be implemented as follows:
                * Note that only one of the two params need to be invoked from the input. 
            
|ParamType|ParamType String| Expected Param | Operation | Verification method |
|---------|----------------|----------------|-----------|---------------------|
|```PARAM.DATE```|"/date"|Various format of date in string, eg. "2020-03-02"| Call ```ledgerList.setIndexToModify()``` to set index of retrieved item. | ```ParamChecker.checkAndReturnDate(packet)```|
|```PARAM.INDEX```|"/index"|Valid index on the list from 1 onwards.|Call ```ledgerList.setIndexToModify()``` to set index of retrieved item. | ```ParamChecker.checkAndReturnIndex(packet)```|

1. __Phase 2: Ledger Deletion__
    1. From ```ManualTracker```, call ```ledgerList.RemoveItemAtCurrIndex()``` to remove the ledger specified by the index set to modify earlier.


![](uml_images/images_updated/manualTrackerDeleteLedgerSeqDiagram.png)

**Entry Tracker: Edit of entries** <br />
The editing of details within the entry is performed in two phases: Entry Retrieval and Entry Edit.
1. __Phase 0: Instruction retrieval__ 
    1. At ```EntryTracker.handleMainMenu()```, the user's input is registered via ```java.util.Scanner``` instance.
    1. Input is parsed by ```InputParser.parseInput()```, and ```ManualTracker.packet``` is set to the returned ```CommandPacket``` instance.
    1. The ```commandString``` of the ```CommandPacket``` instance is evaluated, and the corresponding handle method() is executed.<br>
    In this case, ```handleEditEntry()``` will be called.
1. __Phase 1: Entry retrieval__
    1. At ```handleEditEntry()```, the following processes will be executed:
        1. A singleton instance of ```retrieveentryHandler``` is retrieved. The input String array will be passed into 
        ```retrieveentryHandler.setRequiredParams()``` to set required params for a successful parse.
        1. ```retrieveentryHandler.handlePacket(packet)``` is called to handle params in the packet.
            1. Refer to the section on [Param Handling](#paramHandling) for more details pertaining to general param handling. 
            1. For ```retrieveentryHandler```, the ```handleSingleParam``` abstract method will be implemented as follows:
            
|ParamType|ParamType String| Expected Param | Operation | Verification method |
|---------|----------------|----------------|-----------|---------------------|
|```PARAM.INDEX```|"/index"|Valid index on the list from 1 onwards.|Call ```entryList.setIndexToModify()``` to set index of retrieved item. | ```ParamChecker.checkAndReturnIndex(packet)```|
        
        1. From ```EntryTracker```, call ```entryList.getItemAtCurrIndex``` to retrieve the entry specified by the index set to modify earlier.

1. __Phase 2: Entry edit__
    1. Following Phase 1, the following processes will be executed:
        1. The singleton instance of ```editentryHandler``` is retrieved. There is no need to call ```editentryHandler.setRequiredParams()```
        ; this command does not require params to modify. Instead, it acceps any params supplied and performs the edit accordingly.
        1. `editeEntryHandler.setPacket(packet)` is called to set packet.
    1. ```editentryHandler.handlePacket()``` is called to handle params in the packet.
        1. Refer to the section on [Param Handling](#paramHandling) for more details pertaining to general param handling. 
        1. For ```editentryHandler```, the ```handleSingleParam``` abstract method will be implemented as follows:
            
|ParamType|ParamType String| Expected Param | Operation | Verification method |
|---------|----------------|----------------|-----------|---------------------|
|```PARAM.AMOUNT```|"/amt"|Double in 2 decimal places|Call ```entryList.setAmount()``` to set amount | ```ParamChecker.checkAndReturnDoubleSigned(packet)```|
|```PARAM.TIME```|"/time"|Various format of time in string, eg. "15:00"|Call ```entryList.setTime()``` to set index of retrieved item. | ```ParamChecker.checkAndReturnTime(packet)```|
|```PARAM.INC```|"-i"|Income entry type flag|Call ```entryList.setEntryType(EntryType.INC)``` to set index of retrieved item. | ```nil```|
|```PARAM.EXP```|"-e"|Expense entry type flag|Call ```entryList.setEntryType(EntryType.EXP)``` to set index of retrieved item. | ```nil```|
|```PARAM.DESCRIPTION```|"/desc"|Description in string, ';' character is illegal.|Call ```entryList.setDescription()``` to set index of retrieved item. | ```ParamChecker.checkAndReturnDescription(packet)```|
|```PARAM.CATEGORY```|"/cat"|A set of strings that corresponds with entry type|Call ```entryList.setCategory()``` to set index of retrieved item. | ```ParamChecker.checkAndReturnCategories(packet)```|
            
![](uml_images/images_updated/entryTrackerEditEntrySeqDiagram.png)




### Recurring Tracker
**Overview** <br />
Recurring Tracker handles the creation, deletion and editing of recurring entries.

Entries use the class ```RecurringEntry```, and are stored in the ```RecurringEntryList``` class.

`RecurringEntry` has the following attributes:
* `day` - The day which the transaction occurs
* `description`
* `entryType` - Can be `Constants.EntryType.INC` or `Constants.EntryType.INC` 
depending on whether the entry is an income or expenditure respectively.
* `amount`
* `start` and `end` - Which months does the entry apply to. Set to 1 and 12 by 
default (i.e. occurs every month)
* `isAuto` - Indicates whether the entry is automatically deducted/credited from/to account, 
or manually deducted/credited from/to account
* `notes` - Any user-specified notes

`RecurringTrackerList` extends ItemList, and supports the following methods on top of inherited methods
* `addItem(Item)` - Override. Adds item and sorts according to the day in ascending order
* `getEntriesFromDayXtoY` - Returns an ArrayList of all entries that fall between day X and Y 
(provided by developer in the code, not by user). Mainly used for reminders

**Reminders** <br />
Upon launching the program, the system date and time is recorded in `RunHistory`.

The program then checks if there are any entries upcoming within 5 days from the current date, and prints the entries out
as reminders.

1. Main code calls `MenuPrinter#printReminders()`, which in turn calls 
`ReminderListGenerator#generateListOfRemindersAsStrings()`. 
1. `ReminderListGenerator` checks the current date, and calculates the day of month which is 5 days from current date.
This is stored in `dayToRemindUntil`.
1. `ReminderListGenerator` then checks if `dayToRemindUntil` is after the last day of the current month. If it is,
then the reminder timeframe will overflow to the next month. 
    
    For example:
    * Current date is 29th October. There are 31 days in October. 5 days after today is 34th, 
    which is beyond last day of October.
    * Reminder timeframe will overflow to next month, until 3rd of November

1. If it has overflown, set `isOverflowToNextMonth` to true. Subtract the last day of month from `dayToRemindUntil`.
The new value of `dayToRemindUntil` is the day of next month that the reminder timeframe extends to.

    For example:
    * Continuing from example earlier, `dayToRemindUntil = 34`.
    * `dayToRemindUntil -= NUM_DAYS_IN_OCT`, i.e. 34 - 31
    * `dayToRemindUntil = 3`, representing that the reminder timeframe extends to 3rd of November
1. `ReminderListGenerator` then grabs the entries within the reminder timeframe from the list of all recurring entries.
    * If `isOverflowToNextMonth == true`, it will grab all entries from `currentDay` to `lastDayOfMonth` 
    and all entries from `1` (1st day of next month) to `dayToRemindUntil`
    * Else, it will simply grab all entries from `currentDay` to `dayToRemindUntil`

1. Lastly, the list of entries will be converted to a formatted String to be displayed as reminders, and passed back
to `MenuPrinter`, who will pass it to `UiManager` to print.

The sequence diagram below shows how it works:

![](uml_images/recurringtracker/images/reminderSeqDiagram.png)

<!-- @@author bqxy -->
### FinanceTools
**Overview** <br />
FinanceTools consists of the following features
1. Simple Interest Calculator
2. Yearly/Monthly Compound Interest Calculator
3. Cashback Calculator
4. Miles Credit Calculator
6. Account Storage
7. Command and Calculation History

**Simple Interest Calculator** <br />
Simple Interest Calculator is facilitated by ```SimpleInterest``` class. It allows user to calculate interest earned.
When user inputs ```simple``` as a command, ```handleSimpleInterest``` from ```Handler``` class will handle user
inputted parameters. The calculation is done by ```SimpleInterest``` class. The result is outputted in
```FinanceTools.main()```.
<br />

**Parameters** <br />
* ```/a``` - Amount (Mandatory)
* ```/r``` - Interest Rate (Mandatory)

The following class diagram shows how the Simple Interest Calculator feature works:
<br />
![ClassDiagram](uml_images/financetools/SimpleInterest/SimpleInterestClassDiagram.png)

The following sequence diagram shows how the params are handled before the implementation is carried out:
<br />
For more information on parameters handling, refer [here](#implementation).
![SequenceDiagram1](uml_images/financetools/SimpleInterest/SimpleInterestSequenceDiagram(1).png)
<br />
<br />
The following sequence diagram shows how the Simple Interest Calculator feature works:
<br />
![SequenceDiagram2](uml_images/financetools/SimpleInterest/SimpleInterestSequenceDiagram(2).png)

**Yearly/Monthly Compound Interest Calculator** <br />
Yearly/Monthly Compound Interest Calculator is facilitated by ```YearlyCompoundInterest``` /
```MonthlyCompoundInterest``` class. It allows user to calculate interest earned.
When user inputs ```cyearly``` / ```cmonthly``` as a command, ```handleYearlyCompoundInterest``` /
```handleMonthlyCompoundInterest``` from ```Handler``` class will handle user inputted parameters. The calculation 
is done by ```YearlyCompoundInterest``` / ```MonthlyCompoundInterest``` class. The result is outputted in
```FinanceTools.main()```.
<br />

**Parameters** <br />

* ```/a``` - Amount (Mandatory)
* ```/r``` - Interest Rate (Mandatory)
* ```/p``` - Number of Years/Months (Mandatory)
* ```/d``` - Yearly/Monthly Deposit (Optional)

The following class diagram shows how the Yearly/Monthly Compound Interest Calculator feature works:
<br />
![ClassDiagram1](uml_images/financetools/YearlyMonthlyCompoundInterest/YearlyCompoundInterestClassDiagram.png)
![ClassDiagram2](uml_images/financetools/YearlyMonthlyCompoundInterest/MonthlyCompoundInterestClassDiagram.png)
<br />
The following sequence diagram shows how the params are handled before the implementation is carried out:
<br />
For more information on parameters handling, refer [here](#implementation).
![SequenceDiagram1](uml_images/financetools/YearlyMonthlyCompoundInterest/YearlyCompoundInterestSequenceDiagram(1).png)
<br />
<br />
![SequenceDiagram1](uml_images/financetools/YearlyMonthlyCompoundInterest/MonthlyCompoundInterestSequenceDiagram(1).png)
<br />
<br />
The following sequence diagram shows how the Yearly/Monthly Compound Interest Calculator feature works:
<br />
![SequenceDiagram1](uml_images/financetools/YearlyMonthlyCompoundInterest/YearlyCompoundInterestSequenceDiagram(2).png)
<br />
<br />
![SequenceDiagram1](uml_images/financetools/YearlyMonthlyCompoundInterest/MonthlyCompoundInterestSequenceDiagram(2).png)

**Cashback Calculator** <br />
Cashback Calculator is facilitated by ```Cashback``` class. It allows user to calculate cashback earned.
When user inputs ```cashb``` as a command, ```handleCashback``` from ```Handler``` class will handle user
inputted parameters. The calculation is done by ```Cashback``` class. The result is outputted in
```FinanceTools.main()```.
<br />

**Parameters** <br />
* ```/a``` - Amount (Mandatory)
* ```/r``` - Cashback Rate (Mandatory)
* ```/c``` - Cashback Cap (Mandatory)

The following class diagram shows how the Cashback Calculator feature works:
<br />
![ClassDiagram](uml_images/financetools/Cashback/CashbackClassDiagram.png)

The following sequence diagram shows how the params are handled before the implementation is carried out:
<br />
For more information on parameters handling, refer [here](#implementation).
![SequenceDiagram1](uml_images/financetools/Cashback/CashbackSequenceDiagram(1).png)
<br />
<br />
The following sequence diagram shows how the Cashback Calculator feature works:
<br />
![SequenceDiagram2](uml_images/financetools/Cashback/CashbackSequenceDiagram(2).png)

**Miles Credit Calculator** <br />
Miles Credit Calculator is facilitated by ```MilesCredit``` class. It allows user to calculate miles credit earned.
When user inputs ```miles``` as a command, ```handleMilesCredit``` from ```Handler``` class will handle user
inputted parameters. The calculation is done by ```MilesCredit``` class. The result is outputted in
```FinanceTools.main()```.
<br />

**Parameters** <br />
* ```/a``` - Amount (Mandatory)
* ```/r``` - Miles Rate (Mandatory)

The following class diagram shows how the Miles Credit Calculator feature works:
<br />
![ClassDiagram](uml_images/financetools/MilesCredit/MilesCreditClassDiagram.png)

The following sequence diagram shows how the params are handled before the implementation is carried out:
<br />
For more information on parameters handling, refer [here](#implementation).
![SequenceDiagram1](uml_images/financetools/MilesCredit/MilesCreditSequenceDiagram(1).png)
<br />
<br />
The following sequence diagram shows how the Miles Creidt Calculator feature works:
<br />
![SequenceDiagram2](uml_images/financetools/MilesCredit/MilesCreditSequenceDiagram(2).png)

**Account Storage** <br />
Account Storage feature is facilitated by ```AccountStorage``` class. It allows user to store account
information such as name of account, interest rate, cashback rate, etc. When user inputs ```store``` as command,
```handleAccountStorage``` from ```Handler``` class will handle user inputted parameters and store information 
accordingly. The implementation is done by ```handleInfoStorage``` from ```AccountStorage``` class. Afterwards, this 
information is stored into a txt file which is done by ```updateFile``` from ```AccountSaver``` class.
<br />

Additionally, it implements the following operations:
* ```info``` - list account(s) information
* ```clearinfo``` - clear all information
* ```store /rm <ACCOUNT_NO>``` - delete corresponding account number in list

**Parameters** <br />
* ```/n``` - Account Name (Optional)
* ```/ir``` - Interest Rate (Optional)
* ```/r``` - Cashback Rate (Optional)
* ```/c``` - Cashback Cap (Optional)
* ```/o``` - Other Notes (Optional)
* ```/rm``` - Account Number (Optional)

**Details** <br />
```handleInfoStorage``` stores the user inputted information into an ```ArrayList``` which is then passed into
```updateFile``` to update the txt file. The purpose of using txt file is so that when the user exits and enters the
program again, the information is retained, and the user does not have to re-enter the account information(s) again.
<br />
 
When user first enters FinanceTools in the program, ```readFileContents``` reads 5 lines in the txt file consecutively
in a ```while``` loop because these 5 lines consists of information that belong to a particular account. These
categories include: Name, Interest Rate, Cashback Rate, Cashback Cap and Notes". Doing so helps to facilitate
the ```delete``` option where instead of deleting single lines, we can delete the entire account information
which correspond to a particular account because the information is stored in one index of the ```ArrayList```.
<br />
 
The following class diagram shows how the Account Storage feature works:
<br />
![ClassDiagram](uml_images/financetools/AccountStorage/AccountStorageClassDiagram.png)

The following sequence diagram shows how the params are handled before the implementation is carried out:
<br />
For more information on parameters handling, refer [here](#implementation).
![SequenceDiagram1](uml_images/financetools/AccountStorage/AccountStorageSequenceDiagram(1).png)
<br />
<br />
The following sequence diagram shows how the Account Storage feature works:
<br />

![SequenceDiagram2](uml_images/financetools/AccountStorage/AccountStorageSequenceDiagram(2).png)
<br />
<br />

![SequenceDiagram3](uml_images/financetools/AccountStorage/AccountStorageSequenceDiagram(3).png)
 
**Command and Calculation History** <br />
To store the commands inputted by user and results from calculations in FinanceTools, an ```ArrayList``` is used.
The commands are stored in the ```ArrayList``` before the params are handled and implementation is executed. 
The results from calculation is stored in the ```ArrayList``` when the implementation has finished executed.
<!-- @@author -->

### Goal Tracker
**Set Expense Goal Feature** <br />
The set expense goal feature is being implemented by ```GoalTracker```. It allows the user to set an expense goal for
the respective month to ensure that the user does not overspent his budget. 
When user enter ```expense 2000 for 08```, the command will be sent to InputParser and parse it into String[].
With the String[], it will be sent to a class called ```Goal```, and it will store the individual information.
Afterwards, it will be added to a ArrayList in a class called ```TotalGoalList```. 
 
Not only that, ```GoalTracker``` also implemented a feature called ```set income goal``` that works almost the same as 
set expense goal feature with just slight command difference.
 
 `Format:`
* setExpenseGoal: expense 5000 for 08
* setIncomeGoal: income 5000 for 08
 
**Details** <br />
Firstly, user will input the command based on the `Format`.
Secondly, the input command will be sent to InputParser to parse.
Thirdly, the parsed information will be sent to class `Goal` to store the individual information
Next, it will be added to a ArrayList in class `TotalGoalList`.
Lastly, the goal status will be displayed to the user.  
 
This class diagram will show how the setting of expense goal works:
<br />

![ExpenseClassDiagram](uml_images/goaltracker/SetExpenseGoalClassDiagram.png)
 
This sequence diagram will show the flow of setting of expense goal:
<br />

![ExpenseSequenceDiagram](uml_images/goaltracker/SetExpenseGoalSequenceDiagram.png)

### Storage Utility
**What it does** <br />
Storage utility is a tool designed for backup and storage of all data associated with Goal tracker, Manual tracker and recurring tracker.
It performs auto loading and saving of data upon entry and exit of the program as well as allowing multiple saves to be created and loaded
at will.

**Overview** <br />
Storage utility contains 5 classes. SaveHandler class contains some commonly used functions such as buildFile that is inherited to the 3
saver child classes. goalTrackerSaver produce text file to save goalTracker states, autoTrackerSaver saves recurringTracker states and
manualTrackerSaver saves manualTracker states.

**Save Manager Class Diagram** <br />

![SaveManagerClassDiagram](uml_images/saveManager/SaveManagerClass.png)
<br />
Saver classes alone can handle autosave of data during entry and when exiting the program. This is done by calling load and save functions
in the Financit main. saveManager class adds additional features that performs adding and loading of backup saves. addSave function is done
by calling save function in each respective saver class with 2 parameters attached. Since save function is implemented as variable argument
function, it is designed to accept no argument or two arguments. For the case with no argument the function will save to the default location
given during initilization of the program and used for loading during startup and saving upon exit. For the case with two argument, new directory 
location is specified that is used for saving of backup data.

saveManager loadSave function was implemented by calling first the clear function in each respective saver classes, then the load functions.
FileChannel is also used to copy contents of the backup save file into the default initilzation save file in case program was unexpectedly
terminated.

**Save Manager Sequence Diagram** <br />

![SaveManagerSequenceDiagram](uml_images/saveManager/SequenceSaveManager.png)

# Product scope
## Target user profile

Fresh computing graduates who are just starting to enter the workforce.
* Have limited income/budget
* Little experience in personal financial management
* Busy juggling their various job applications and interview which might cause them to lose track of their expenditure/ 
bill payments
* First time drawing salary and lack experience in income tax filling

## Value proposition
 
**Expenditure Tracker**
* Input itemised spending on a daily basis
* Sum the monthly/weekly expenditure, as well as average weekly/daily expenditure
* Categorise expenditures (e.g. food, transport etc) and sort by category

**Budget Management**
* Set a budget (monthly)
* Remind users of how much budget they have left for that month
* Edit the budget (monthly)
* Display the budget for that month

**Bill Tracker**
* Remind users of payment due dates
* Monthly tracker for each individual bill, visualise trends in bill spending

**Finance Tools**
* Calculate simple interest
* Calculate compound interest with optional monthly/yearly deposit
* Calculate cashback earned
* Calculate miles credit earned
* Save account information for reference

# User Stories

|Version| As a ... | I want to ... | So that I can ...|
|--------|----------|---------------|------------------|
|v1.0|user|calculate interest over a principal amount|know how much interest I can earn|
|v1.0|user|calculate interest earned over a period time|know how much interest I can earn at the end of a period|
|v1.0|user|calculate cashback earned|know how much cashback I can earn|
|v1.0|user|calculate miles credit earned|know how much miles credit I can earn|
|v1.0|user|set expense goal for 1 year|manage my expenditure according to the budget I set aside|
|v1.0|user|set income goal for 1 year|know how much I have saved and did I reach my saving target|
|v2.0|user|calculate interest over a principal amount with yearly or monthly deposit|know how much interest I can earn with regular deposits|
|v2.0|user|store account or card information|refer to account features such as interest rate any time|
|v2.0|user|compare my calculations with different interest rate|decide which account is better|
|v2.0|user|set expense goal for specific month|manage my expenditure monthly instead of yearly|
|v2.0|user|set income goal for specific month|know exactly which month I manage to saved up to my target goal|
|v2.0|user|edit expense/income goal for specific month|adjust my expenditure/saving target according to the situation|
|v2.0|user|display expense/income goal for specific month|keep track of my progress|

# Non-Functional Requirements

* _Constraint_ - Single User Product
* _Performance_ - JAR file does not exceed 100Mb
* _User_ - Users should prefer typing on CLI
* _Program_ - Platform independent (Windows/Mac/Linux)
* _Program_ - Works without needing an installer

# Glossary

* _IntelliJ_ - An Integrated Development Environment (IDE) used to develop FinanceIt
* _CLI_ - Command Line Interface
* _UML_ - Unified Modeling Language

# Instructions for Manual Testing

1. Download the executable from our [latest release](https://github.com/AY2021S1-CS2113-T16-1/tp/releases/)
1. Save the executable file in your preferred folder
1. Run the program via the command line. The command is: ```java -jar financeit.jar```
1. You should see the following output:

![](developerGuide_images/screenshots_mainmenu/main_menu.png)

## Main Menu
1. Accessing a feature:
    1. ```ManualTracker```
        1. Enter ```manual``` into the console.
            You should see the following: 
            
    ![](developerGuide_images/screenshots_mainmenu/main_menu_manual.png)

    1. ```RecurringTracker```
    1. ```GoalTracker```
    1. ```SaveManager```
    1. ```FinanceTools```

1. Exiting the main menu and quit the program: 
    1. Enter ```exit``` into the console.
        You should see the following: 
        
![](developerGuide_images/screenshots_mainmenu/main_menu_exit.png)

## ManualTracker
**Show Command List** <br />
1. Enter ```commands``` into the console.
You should see the following: 

![](developerGuide_images/screenshots_manualtracker/manual_commands.png)

**Create Ledger** <br />
**Positive Test** <br />
1. Enter ```new /date 200505``` into the console.
You should see the following:

![](developerGuide_images/screenshots_manualtracker/manual_new.png)

**Negative test: Duplicate inputs** <br />
1. Again, enter ```new /date 200505``` into the console.
You should see the following:

![](developerGuide_images/screenshots_manualtracker/manual_new_dup.png)

**Testing Show Ledger List** <br />
**Positive Test** <br />
1. Enter ```list``` into the console. 
You should see the following: 

![](developerGuide_images/screenshots_manualtracker/manual_list.png)

    * Observe that there is currently one ledger in the list, of date 2020-05-05.
1. Refer to [7.2.1](#7.2.1) to create another ledger of date 2020-06-06 using the command: 
```new /date 200606```. 
1. Enter ```list``` into the console. 
    * Observe that there are now two ledgers in the list.
You should see the following: 

![](developerGuide_images/screenshots_manualtracker/manual_list2.png)

**Testing Delete Ledger** <br />
**Positive Test** <br />
1. Enter ```delete /id 1``` into the console.
    * This will delete the first ledger on index, which is of date 2020-05-05
1. Enter ```list``` into the consolde.
You should see the following:

![](developerGuide_images/screenshots_manualtracker/manual_delete1.png)

    * Observe there is now one ledger on the list.

**Open Ledger** <br />
1. Enter ```open /date 200707``` into the console.
You should see the following:

![](developerGuide_images/screenshots_manualtracker/manual_open.png)

    * Note that the ledger of date 2020-07-07 was not created beforehand. 
    However, the ledger will be automatically created by the operation, and
    will resume as per normal. 

## EntryTracker
1. The following testing guide assumes that testing at [7.2](#7.2) is completed.
**Show Command List** <br />
1. Enter ```commands``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_commands.png)

**Show Category List** <br />
1. Enter ```cat``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_cat.png)

**Create Entry** <br />
1. Enter ```new /time 1500 /cat tpt /amt $16.30 /desc Riding the bus back home -e``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_create.png)
1. Enter ```new /time 1500 /cat slr /amt $16.30 /desc Riding the bus back home -i``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_create2.png)

1. Enter ```new /time 1500 /cat tpt /amt $16.30 /desc Riding the bus back home -i``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_create_err1.png)
    * Note that the error is thrown because category ```tpt``` is not considered an income, `-i`. Instead, it is 
    considered an expenditure, and `-e` should have been used instead.

**Testing Show Entry List** <br />
1. Enter ```list``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_list.png)
    * Note that the number of entries is now __2__.

**Testing Edit Entry** <br />

1. Enter ```edit /id 1 /amt $0.50``` into the console.
1. Enter ```list``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_edit_list.png)

* Observe that the entry of entry number 1 is not $0.50 under the __Amount__ column.

**Testing Delete Entry** <br />
1. Enter ```delete /id 2``` into the console.
1. Enter ```list``` into the console.
You should see the following:

![](developerGuide_images/screenshots_entrytracker/entry_delete_list.png)

* Observe the entry that is the latter to be added, entry with __Entry Type = Income__, is now
removed from the list.

## RecurringTracker
1. Enter `recur` in the Main Menu. You should see the following:
![](developerGuide_images/screenshots_recurringtracker/enter_tracker.png)
**Show Command List** <br />
1. Enter `commands`. Output:
![](developerGuide_images/screenshots_recurringtracker/commands.png)
**Testing Add Entry** <br />
**Positive Test 1: Normal Entry** <br />
1. Enter `add -e /desc Netflix /amt 36.20 /day 27 /notes Cancel when finished watching Black Mirror`. Output:
![](developerGuide_images/screenshots_recurringtracker/add_entry_all_months.png)

**Entry with special day of month** <br />
1. Enter `add -e /desc Drinks /amt 58.45 /day 31`. Output:
![](developerGuide_images/screenshots_recurringtracker/add_entry_day_31.png)

**Negative Test** <br />
1. Enter `add /desc OIH()(&%* /amt 343243`. Output:
![](developerGuide_images/screenshots_recurringtracker/add_entry_no_day_i&e.png)

**Testing List Entries** <br />
* The following testing guide assumes that the testing of show command list is completed. <br />
Enter `list`. Output:
![](developerGuide_images/screenshots_recurringtracker/list.png)

**Testing Edit Entry** <br />
* The following testing guide assumes that the testing of show command list is completed. <br />
**Positive Test** <br />
1. Enter `edit /id 1 /day 29 -i`. Output:
![](developerGuide_images/screenshots_recurringtracker/edit_entry.png)
1. Enter `list`. Output:
![](developerGuide_images/screenshots_recurringtracker/list_after_edit.png)

**Negative Test: No Params to Edit** <br />
1. Enter `edit /id 1`. Output:

![](developerGuide_images/screenshots_recurringtracker/edit_entry_no_params.png)
<br />
**Negative Test: No Such Index** <br />
1. Enter `edit /id 4 /desc Hello Bubble`. Output:
![](developerGuide_images/screenshots_recurringtracker/edit_entry_invalid_index.png)

**Testing Delete Entry** <br />
* The following testing guide assumes that the testing of show command list is completed. <br />
1. Enter `delete /id 2`. Output:
![](developerGuide_images/screenshots_recurringtracker/delete_entry.png)
1. Enter `list`. Output:
![](developerGuide_images/screenshots_recurringtracker/list_after_delete.png)

**Testing Reminders** <br />
* The following testing guide assumes that all previous entries have been deleted. This can be achieved by running `delete /id 1` repeatedly until list is empty.
* As reminders are based on system date at time of running, the `/day` param of the below examples will have to be modified accordingly. Simply copy-pasting the commands will not create the expected output.
1. Enter `add -e /desc SingTel bill /amt 120.50 /day {CURRENT_DAY}`
    * E.g. if today is 15th, input will be `/day 15`
    
1. Enter `add -e -auto /desc Spotify subscription /amt 9.99 /day {CURRENT_DAY + 2}`
    * E.g. if today is 15th, input will be `/day 17`
    
1. Enter `add -i /desc Collect cash from Sonia /amt 500 /day {CURRENT_DAY + 7}`
    * E.g. if today is 15th, input will be `/day 22`
    * E.g. if today is 28th, input will be `/day 5` OR `day 4` depending on whether the current month has 30 or 31 days respectively.

1. Enter `exit` to quit to main menu. Reminders are printed above the Main Menu prompt. Note: Screenshot was taken on 6th, hence entries entered above are on the 6th, 8th and 13th respective.

    Output:
![](developerGuide_images/screenshots_recurringtracker/reminders.png)

1. Enter `exit` to quit the program. Run the .jar file again. Reminders are printed below the logo and above the Main Menu prompt.
![](developerGuide_images/screenshots_recurringtracker/reminders_launch.png)

## GoalTracker
**Set Goal for Expense** <br />
**Positive Test** <br />
Enter ``` expense 4000 for 01 ``` into the console.
You should see the following: 
![SetExpenseGoal](developerGuide_images/screenshot_goaltracker/SetExpenseGoal.png)

**Negative Test** <br />
Again, enter ```expense 2000 for 01``` into the console.
You should see the following:
![NegativeSet](developerGuide_images/screenshot_goaltracker/NegativeSetExpense.png)

**Testing Set Goal for Income** <br />
**Positive Test** <br />
Enter ```income 2000 for 02``` into the console.
You should see the following:
![SetIncomeGoal](developerGuide_images/screenshot_goaltracker/SetIncomeGoal.png)

**Negative Test** <br />
Again, enter ```income 7000 for 02``` into the console.
You should see the following:
![NegativeSetIncome](developerGuide_images/screenshot_goaltracker/NegativeSetIncome.png)

**Edit Goal for Expense** <br />
**Positive Test** <br />
Enter ```edit expense 2000 for 01``` into the console.
You should see the following:
![EditExpenseGoal](developerGuide_images/screenshot_goaltracker/EditExpenseGoal.png)

**Negative Test** <br />
Enter ```edit expense 4000 for 05``` into the console.
You should see the following:
![NegativeEditExpense](developerGuide_images/screenshot_goaltracker/NegativeEditExpense.png)

**Edit Goal for Income** <br />
**Positive Test** <br />
Enter ```edit income 5000 for 02``` into the console.
You should see the following:
![EditIncomeGoal](developerGuide_images/screenshot_goaltracker/EditIncomeGoal.png)

**Negative Test** <br />
Enter ```edit income 1000 for 09``` into the console.
You should see the following:
![NegativeEditIncome](developerGuide_images/screenshot_goaltracker/NegativeEditIncome.png)

**Display Expense goal** <br />
**Positive Test** <br />
Enter ```display expense for 01``` into the console.
You should see the following:
![DisplayExpenseGoal](developerGuide_images/screenshot_goaltracker/DisplayExpenseGoal.png)

**Display Income Goal** <br />
**Positive Test** <br />
Enter ```display income for 02``` into the console.
You should see the following:
![DisplayIncomeGoal](developerGuide_images/screenshot_goaltracker/DisplayIncomeGoal.png)

**Integrate Goal Tracker with Recurring Tracker** [Coming in v3.0] <br />
In the next version, goal tracker will be used to keep track not only the manual tracker but also the recurring 
tracker. With this feature being implemented, those fixed monthly income and expenditure will also be included into 
the goal tracker progress to better aid the user in managing their finances.

## SaveManager
**Add Save** <br />
**Positive Test** <br />
Enter ```add /name save123``` into the console.
<br />
You should see the following:

![capture](uml_images/saveManager/puml/Capture.PNG)

![capture2](uml_images/saveManager/puml/Capture2.PNG)

**Negative Test** <br />
Enter ```add /name``` into the console.
<br />
You should see the following:

![capture1](uml_images/saveManager/puml/Capture1.PNG)

**Load Save** <br />
**Positive Test** <br />
Enter ```load /name save123``` into the console.
<br />
You should see the following:

![capture3](uml_images/saveManager/puml/Capture3.PNG)

**Negative Test** <br />
Enter ```load /name wrongName``` into the console.
<br />
You should see the following:

![capture4](uml_images/saveManager/puml/Capture4.PNG)

**Delete Save** <br />
**Positive Test** <br />
Enter ```delete /name save123``` into the console.
<br />
You should see the following:

![capture5](uml_images/saveManager/puml/Capture5.PNG)

![capture7](uml_images/saveManager/puml/Capture7.PNG)

**Negative Test** <br />
Enter ```delete /name wrongName``` into the console.
<br />
You should see the following:

![capture6](uml_images/saveManager/puml/Capture6.PNG)

<!-- @@author bqxy -->
## FinanceTools
**Simple Interest Calculator** <br />
Enter ```simple /a 1000 /r 5``` into the console. <br />
You should see the following:
![Example](screenshots/financetools/SimpleInterest(1).PNG)

**Yearly Compound Interest Calculator** <br />
Enter ```cyearly /a 1000 /r 3 /p 2``` into the console. <br />
You should see the following:
![Example](screenshots/financetools/YearlyCompoundInterest(1).PNG) <br />

**Monthly Compound Interest Calculator** <br />
Enter ```cmonthly /a 1000 /r 3 /p 2``` into the console. <br />
You should see the following:
![Example](screenshots/financetools/MonthlyCompoundInterest(1).PNG) <br />

**Cashback Calculator** <br />
Enter ```cashb /a 1000 /r 5 /c 1000``` into the console. <br />
You should see the following:
![Example](screenshots/financetools/Cashback(1).PNG) <br />

**Miles Credit Calculator** <br />
Enter ```miles /a 1000 /r 5``` into the console. <br />
You should see the following:
![Example](screenshots/financetools/Miles(1).PNG) <br />

**Account Storage** <br />
Enter ```store /n myaccount /ir 2 /r 2 /c 100 /o main account``` followed by ```info``` into the console. <br />
You should see the following:
![Example](screenshots/financetools/AccountStorage(1).PNG)
<br />
<br />
![Example](screenshots/financetools/AccountStorage(2).PNG) <br />
<!-- @@author -->
