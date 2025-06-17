import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class CrudGuiApp extends Application {

    private final String url = "jdbc:oracle:thin:@localhost:1521:XE";
    private final String user = "system";
    private final String pass = "991982";

    private VBox mainContainer;
    private TextArea outputArea;
    private static final String TABLE_TAG_MARKER = "--@table:";

    private static final List<String> TABLE_NAMES = List.of(
            "CUSTOMERS", "CATEGORIES", "PRODUCTS", "ORDERS", "ORDER_ITEMS"
    );

    private final ObservableSet<Map<String, Object>> dirtyRows = FXCollections.observableSet();
    private String currentOpenTableName = null;
    private String currentPrimaryKeyColumn = null;

    private VBox navPane;
    private BorderPane rootLayout;

    private final Map<String, List<Map<String, Object>>> originalDataCache = new HashMap<>();

    // The code from start() to processSelectForTable() is unchanged
    @Override
    public void start(Stage stage) {
        stage.setTitle("Database Management System");

        // --- Top Bar ---
        StackPane topBar = new StackPane();
        topBar.setPadding(new Insets(10, 25, 10, 25));
        topBar.setStyle("-fx-border-color: #D0D0D0; -fx-border-width: 0 0 1 0;");

        Button menuButton = new Button("\u2630 MENU");
        menuButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5 15 5 15; -fx-background-color: #E0E0E0;");

        Label appTitleLabel = new Label("Database Management System");
        appTitleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        topBar.getChildren().addAll(appTitleLabel, menuButton);
        StackPane.setAlignment(menuButton, Pos.CENTER_LEFT);
        StackPane.setAlignment(appTitleLabel, Pos.CENTER);

        // --- Create Navigation Buttons ---
        String buttonStyle = "-fx-font-size: 14px; -fx-min-height: 40px; " +
                "-fx-background-radius: 8; -fx-text-fill: white; -fx-font-weight: bold;";

        Button createBtn = new Button("CREATE Tables");
        createBtn.setStyle(buttonStyle + "-fx-background-color: #4CAF50;");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> showGenericForm("CREATE", "create.txt"));

        Button dropBtn = new Button("DROP Tables");
        dropBtn.setStyle(buttonStyle + "-fx-background-color: #B71C1C;");
        dropBtn.setMaxWidth(Double.MAX_VALUE);
        dropBtn.setOnAction(e -> showDropForm());

        Button insertBtn = new Button("INSERT Data");
        insertBtn.setStyle(buttonStyle + "-fx-background-color: #2196F3;");
        insertBtn.setMaxWidth(Double.MAX_VALUE);
        insertBtn.setOnAction(e -> showGenericForm("INSERT", "insert.txt"));

        Button deleteBtn = new Button("DELETE Data");
        deleteBtn.setStyle(buttonStyle + "-fx-background-color: #F44336;");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.setOnAction(e -> showGenericForm("DELETE", "delete.txt"));

        Button selectBtn = new Button("SELECT & UPDATE Data");
        selectBtn.setStyle(buttonStyle + "-fx-background-color: #9C27B0;");
        selectBtn.setMaxWidth(Double.MAX_VALUE);
        selectBtn.setOnAction(e -> showSelectForm());

        Button sqlEditorBtn = new Button("SQL Editor");
        sqlEditorBtn.setStyle(buttonStyle + "-fx-background-color: #607D8B;");
        sqlEditorBtn.setMaxWidth(Double.MAX_VALUE);
        sqlEditorBtn.setOnAction(e -> showSqlEditor());

        // --- Configure the Accordion-based Navigation Pane (Sidebar) ---
        VBox ddlButtons = new VBox(10, createBtn, dropBtn);
        TitledPane ddlPane = new TitledPane("DDL (Definition)", ddlButtons);

        VBox dmlButtons = new VBox(10, insertBtn, deleteBtn, selectBtn);
        TitledPane dmlPane = new TitledPane("DML (Manipulation)", dmlButtons);

        VBox toolsButtons = new VBox(10, sqlEditorBtn);
        TitledPane toolsPane = new TitledPane("Tools", toolsButtons);

        Accordion navAccordion = new Accordion();
        navAccordion.getPanes().addAll(ddlPane, dmlPane, toolsPane);
        navAccordion.setExpandedPane(ddlPane);

        navPane = new VBox(navAccordion);
        navPane.setPadding(new Insets(10));
        navPane.setStyle("-fx-background-color: #f5f5f5;");
        VBox.setVgrow(navAccordion, Priority.ALWAYS);
        navPane.setPrefWidth(250);

        // --- Configure the Main Content Area ---
        mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(15));
        mainContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CFD8DC; -fx-border-width: 1; -fx-border-radius: 8;");
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setStyle("-fx-font-size: 14px; -fx-font-family: 'Monospaced'; -fx-control-inner-background: #ffffff; -fx-border-color: #CFD8DC; -fx-border-width: 1; -fx-border-radius: 8;");
        outputArea.setWrapText(true);
        VBox outputColumn = new VBox(5, new Label("Application Output:"), outputArea);
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        HBox centerContent = new HBox(20, mainContainer, outputColumn);
        centerContent.setPadding(new Insets(20, 20, 20, 20));
        HBox.setHgrow(outputColumn, Priority.ALWAYS);
        HBox.setHgrow(mainContainer, Priority.ALWAYS);

        menuButton.setOnAction(e -> toggleNavPane());

        // --- The Root Layout using BorderPane ---
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #f5f5f5;");
        rootLayout.setTop(topBar);
        rootLayout.setCenter(centerContent);
        rootLayout.setLeft(navPane);

        Scene scene = new Scene(rootLayout, 1300, 800);

        String accordionCss = """
            .accordion .titled-pane > .title {
                -fx-background-color: #ECEFF1, linear-gradient(from 0px 0px to 0px 5px, #F5F5F5, #E0E0E0);
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #333;
                -fx-border-color: #BDBDBD;
                -fx-border-width: 1px 0px 1px 0px;
            }
            .accordion .titled-pane:hover > .title {
                -fx-background-color: #CFD8DC;
            }
            .accordion .titled-pane > .title .arrow {
                -fx-background-color: #37474F;
                -fx-shape: "M 0 0 L 4 4 L 8 0 Z";
            }
            .accordion .titled-pane > .content {
                -fx-background-color: white;
                -fx-border-color: #BDBDBD;
                -fx-border-width: 0px 1px 1px 1px;
                -fx-padding: 15;
            }
            .accordion .titled-pane:focused {
                -fx-text-box-border: transparent;
            }
        """;
        scene.getStylesheets().add("data:text/css;base64," + Base64.getEncoder().encodeToString(accordionCss.getBytes()));

        stage.setScene(scene);
        stage.show();

        showGenericForm("CREATE", "create.txt");
        outputArea.setText("Database Management System Ready!\n");
    }

    private void toggleNavPane() {
        if (rootLayout.getLeft() == null) {
            rootLayout.setLeft(navPane);
        } else {
            rootLayout.setLeft(null);
        }
    }

    private void showSelectForm() {
        clearMainContainer();
        Label titleLabel = new Label("SELECT & UPDATE");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #9C27B0;");
        TextField manualTableNameField = new TextField();
        ToggleGroup tableToggleGroup = new ToggleGroup();
        VBox inputControls = createTableInputControls(manualTableNameField, tableToggleGroup);
        Button selectDataBtn = new Button("Fetch Data from select.txt");
        selectDataBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        selectDataBtn.setMaxWidth(Double.MAX_VALUE);

        Accordion resultAccordion = new Accordion();
        VBox resultsAndButtonBox = new VBox(10);
        VBox.setVgrow(resultAccordion, Priority.ALWAYS);

        selectDataBtn.setOnAction(e -> {
            List<String> tablesToProcess = getTablesToProcess(manualTableNameField, tableToggleGroup);
            if (tablesToProcess.isEmpty()) {
                outputArea.appendText("❌ Error: Please select a table.\n\n");
                return;
            }
            resultsAndButtonBox.getChildren().clear();
            resultAccordion.getPanes().clear();
            dirtyRows.clear();
            originalDataCache.clear();

            String fileContent = readQueryFromFile("select.txt");
            if (fileContent == null) return;

            for (String tableName : tablesToProcess) {
                processSelectForTable(tableName, fileContent, resultAccordion, resultsAndButtonBox);
            }
            if (!resultAccordion.getPanes().isEmpty()) {
                resultAccordion.setExpandedPane(resultAccordion.getPanes().get(0));
            }
        });

        mainContainer.getChildren().addAll(
                titleLabel, inputControls, selectDataBtn, new Separator(),
                new Label("Query Results (double-click cell to edit):"), resultAccordion, resultsAndButtonBox
        );
    }

    private void processSelectForTable(String tableName, String fileContent, Accordion accordion, VBox buttonContainer) {
        String queryBlock = findQueryBlockForTable(fileContent, tableName);
        if (queryBlock == null) {
            outputArea.appendText("❌ Error: No query block for '" + tableName + "' in select.txt.\n");
            return;
        }
        String finalSql = findFirstStatement(queryBlock);
        if (finalSql.isEmpty()) {
            outputArea.appendText("❌ Error: No executable statement for '" + tableName + "'.\n");
            return;
        }
        try (Connection con = DriverManager.getConnection(url, user, pass);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(finalSql)) {

            TableView<Map<String, Object>> resultTable = createEditableTableView(rs, tableName, buttonContainer, accordion);
            TitledPane pane = new TitledPane(tableName + " (" + resultTable.getItems().size() + " rows)", resultTable);
            accordion.getPanes().add(pane);

        } catch (SQLException ex) {
            outputArea.appendText("❌ Database error for " + tableName + ": " + ex.getMessage() + "\n\n");
        }
    }


    // --- THIS METHOD CONTAINS THE PRIMARY CHANGE ---
    private TableView<Map<String, Object>> createEditableTableView(ResultSet rs, String tableName, VBox buttonContainer, Accordion accordion) throws SQLException {
        TableView<Map<String, Object>> resultTable = new TableView<>();
        this.currentPrimaryKeyColumn = getPrimaryKeyColumnName(tableName);

        if (this.currentPrimaryKeyColumn == null) {
            resultTable.setEditable(false);
        } else {
            resultTable.setEditable(true);
            this.currentOpenTableName = tableName.toUpperCase();
        }

        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            final String columnName = metaData.getColumnName(i).toUpperCase();
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(new MapValueFactory(columnName));

            if (this.currentPrimaryKeyColumn != null) {
                column.setCellFactory(TextFieldTableCell.forTableColumn());

                column.setOnEditCommit(event -> {
                    String newValue = event.getNewValue();
                    Map<String, Object> row = event.getRowValue();

                    // --- NEW VALIDATION LOGIC FOR NAMES AND EMAILS ---
                    
                    // Check Rule 1: Names cannot be empty
                    if ((columnName.equalsIgnoreCase("FIRST_NAME") || columnName.equalsIgnoreCase("LAST_NAME")) && isNullOrBlank(newValue)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Name");
                        alert.setHeaderText("Validation Failed");
                        alert.setContentText("First Name and Last Name cannot be empty.");
                        alert.showAndWait();
                        resultTable.refresh(); // Cancel the edit
                        
                    // Check Rule 2: Email must be a valid @gmail.com address
                    } else if (columnName.equalsIgnoreCase("EMAIL") && !isValidGmail(newValue)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Email");
                        alert.setHeaderText("Validation Failed");
                        alert.setContentText("The email address for a customer must end with '@gmail.com'.");
                        alert.showAndWait();
                        resultTable.refresh(); // Cancel the edit
                        
                    // If all rules pass, proceed
                    } else {
                        row.put(columnName, newValue);
                        dirtyRows.add(row);
                        outputArea.appendText("ℹ️ Staged update for row with PK = " + row.get(currentPrimaryKeyColumn) + "\n");
                    }
                });

                if (columnName.equalsIgnoreCase(this.currentPrimaryKeyColumn)) {
                    column.setEditable(false);
                    column.setStyle("-fx-font-weight: bold; -fx-background-color: #f0f0f0;");
                }
            }
            resultTable.getColumns().add(column);
        }

        List<Map<String, Object>> originalRows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String colName = metaData.getColumnName(i).toUpperCase();
                row.put(colName, rs.getObject(i) != null ? rs.getObject(i).toString() : "NULL");
            }
            resultTable.getItems().add(row);
            originalRows.add(new HashMap<>(row));
        }
        originalDataCache.put(tableName.toUpperCase(), originalRows);

        if(this.currentPrimaryKeyColumn != null) {
            addDirtyStateListener(buttonContainer, resultTable, accordion);
        } else {
            Label warning = new Label("⚠️ Updates disabled: No Primary Key found for " + tableName);
            warning.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-padding: 5;");
            buttonContainer.getChildren().add(warning);
        }

        return resultTable;
    }
    
    // --- HELPER METHODS FOR CLIENT-SIDE VALIDATION ---
    private boolean isValidGmail(String email) {
        if (email == null) return false; // Or true if NULL is allowed
        return email.toLowerCase().trim().endsWith("@gmail.com");
    }

    private boolean isNullOrBlank(String s) {
        return s == null || s.trim().isBlank();
    }


    // --- The rest of the code is unchanged ---
    private void addDirtyStateListener(VBox buttonContainer, TableView<Map<String, Object>> table, Accordion accordion) {
        Button commitBtn = new Button("Commit All Updates");
        commitBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        commitBtn.setMaxWidth(Double.MAX_VALUE);
        commitBtn.setOnAction(e -> commitAllUpdates());

        Button revertBtn = new Button("Revert Changes");
        revertBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #757575; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        revertBtn.setMaxWidth(Double.MAX_VALUE);
        revertBtn.setOnAction(e -> {
            String activeTable = getActiveTableName(accordion);
            if (activeTable != null) {
                revertChanges(table, activeTable);
            }
        });

        HBox actionButtons = new HBox(10, commitBtn, revertBtn);

        dirtyRows.addListener((SetChangeListener<Map<String, Object>>) change -> {
            if (!dirtyRows.isEmpty()) {
                if (!buttonContainer.getChildren().contains(actionButtons)) {
                    buttonContainer.getChildren().add(actionButtons);
                }
            } else {
                buttonContainer.getChildren().remove(actionButtons);
            }
        });
    }

    private void revertChanges(TableView<Map<String, Object>> table, String tableName) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Revert all unsaved changes for table '" + tableName + "'?", ButtonType.YES, ButtonType.NO);
        if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            List<Map<String, Object>> originalRows = originalDataCache.get(tableName.toUpperCase());
            if (originalRows != null) {
                table.getItems().setAll(originalRows);
                dirtyRows.clear();
                table.refresh();
                outputArea.appendText("ℹ️ Changes for " + tableName + " have been reverted.\n\n");
            }
        }
    }

    private String getActiveTableName(Accordion accordion) {
        TitledPane expandedPane = accordion.getExpandedPane();
        if (expandedPane != null) {
            String title = expandedPane.getText();
            return title.split(" \\(")[0];
        }
        return null;
    }

    private void commitAllUpdates() {
        if (dirtyRows.isEmpty() || currentOpenTableName == null || currentPrimaryKeyColumn == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Save " + dirtyRows.size() + " change(s) to " + currentOpenTableName + "?");
        if(confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            outputArea.appendText("ℹ️ Update cancelled by user.\n\n");
            return;
        }

        int successCount = 0;
        int failureCount = 0;
        List<Map<String, Object>> successfullyCommittedRows = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            con.setAutoCommit(false);
            for (Map<String, Object> row : dirtyRows) {
                Object pkValue = row.get(currentPrimaryKeyColumn.toUpperCase());
                StringBuilder setClause = new StringBuilder();
                List<Object> params = new ArrayList<>();
                row.forEach((key, value) -> {
                    if (!key.equalsIgnoreCase(currentPrimaryKeyColumn)) {
                        if (!params.isEmpty()) setClause.append(", ");
                        setClause.append(key).append(" = ?");
                        params.add(value.equals("NULL") ? null : value);
                    }
                });
                String sql = "UPDATE " + currentOpenTableName + " SET " + setClause + " WHERE " + currentPrimaryKeyColumn + " = ?";
                params.add(pkValue);
                try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                    for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
                    pstmt.executeUpdate();
                    successCount++;
                    successfullyCommittedRows.add(row);
                } catch (SQLException e) {
                    failureCount++;
                    outputArea.appendText("❌ FAILED to update row with " + currentPrimaryKeyColumn + "=" + pkValue + ": " + e.getMessage() + "\n");
                }
            }
            if (failureCount > 0) {
                con.rollback();
                outputArea.appendText("❌ Batch update failed due to " + failureCount + " error(s). All changes rolled back.\n\n");
            } else {
                con.commit();
                outputArea.appendText("✅ Successfully updated " + successCount + " row(s) in " + currentOpenTableName + ".\n\n");
                originalDataCache.get(currentOpenTableName).replaceAll(row -> {
                    Object pk = row.get(currentPrimaryKeyColumn.toUpperCase());
                    return successfullyCommittedRows.stream()
                            .filter(dirty -> dirty.get(currentPrimaryKeyColumn.toUpperCase()).equals(pk))
                            .findFirst()
                            .orElse(row);
                });
                dirtyRows.clear();
            }
        } catch (SQLException e) {
            outputArea.appendText("❌ DB Connection or Commit Error: " + e.getMessage() + "\n");
        }
    }

    private void clearMainContainer() {
        mainContainer.getChildren().clear();
        mainContainer.setAlignment(Pos.TOP_LEFT);
    }

    private void showGenericForm(String title, String queryFileName) {
        clearMainContainer();
        Map<String, String> colors = Map.of("CREATE", "#4CAF50", "INSERT", "#2196F3", "DELETE", "#F44336");
        String colorHex = colors.getOrDefault(title, "#607D8B");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");
        TextField manualTableNameField = new TextField();
        ToggleGroup tableToggleGroup = new ToggleGroup();
        VBox inputControls = createTableInputControls(manualTableNameField, tableToggleGroup);
        Button executeBtn = new Button("Execute from " + queryFileName);
        executeBtn.setStyle("-fx-font-size: 14px; -fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        executeBtn.setMaxWidth(Double.MAX_VALUE);
        executeBtn.setOnAction(e -> {
            List<String> tablesToProcess = getTablesToProcess(manualTableNameField, tableToggleGroup);
            if (tablesToProcess.isEmpty()) {
                outputArea.appendText("❌ Error: Please enter a name or select a table.\n\n");
                return;
            }
            String fileContent = readQueryFromFile(queryFileName);
            if (fileContent != null) processFileOperations(tablesToProcess, fileContent, queryFileName);
        });
        mainContainer.getChildren().addAll(titleLabel, new Label("Enter a name OR select a single table:"), inputControls, executeBtn);
    }

    private VBox createTableInputControls(TextField manualEntryField, ToggleGroup tableToggleGroup) {
        VBox container = new VBox(15);
        Label manualEntryLabel = new Label("Enter a Single Table Name:");
        manualEntryField.setPromptText("e.g., PRODUCTS");
        Label radioLabel = new Label("Or Select a Single Table From The List:");
        VBox radioButtonsContainer = new VBox(5);
        radioButtonsContainer.setPadding(new Insets(0, 0, 0, 10));
        for (String tableName : TABLE_NAMES) {
            RadioButton rb = new RadioButton(tableName);
            rb.setToggleGroup(tableToggleGroup);
            radioButtonsContainer.getChildren().add(rb);
        }
        ScrollPane sp = new ScrollPane(radioButtonsContainer);
        sp.setFitToWidth(true);
        sp.setPrefHeight(120);
        sp.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        Button clearSelectionBtn = new Button("Clear Selection");
        clearSelectionBtn.setOnAction(e -> {
            if (tableToggleGroup.getSelectedToggle() != null) {
                tableToggleGroup.getSelectedToggle().setSelected(false);
            }
        });
        tableToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean isSelected = newToggle != null;
            manualEntryField.setDisable(isSelected);
            if (isSelected) manualEntryField.clear();
        });
        manualEntryField.textProperty().addListener((obs, oldText, newText) -> {
            boolean hasText = newText != null && !newText.trim().isEmpty();
            radioButtonsContainer.setDisable(hasText);
            clearSelectionBtn.setDisable(hasText);
            if (hasText && tableToggleGroup.getSelectedToggle() != null) {
                tableToggleGroup.getSelectedToggle().setSelected(false);
            }
        });
        container.getChildren().addAll(
                manualEntryLabel, manualEntryField, new Separator(), radioLabel, sp, clearSelectionBtn
        );
        return container;
    }

    private void showDropForm() {
        clearMainContainer();
        Label titleLabel = new Label("DROP");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #B71C1C;");
        TextField manualTableNameField = new TextField();
        ToggleGroup tableToggleGroup = new ToggleGroup();
        VBox inputControls = createTableInputControls(manualTableNameField, tableToggleGroup);
        Button executeBtn = new Button("DROP Selected Table");
        executeBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #B71C1C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        executeBtn.setMaxWidth(Double.MAX_VALUE);
        executeBtn.setOnAction(e -> {
            List<String> tablesToProcess = getTablesToProcess(manualTableNameField, tableToggleGroup);
            if (tablesToProcess.isEmpty()) {
                outputArea.appendText("❌ Error: Please select a table to drop.\n\n");
                return;
            }
            String tableToDrop = tablesToProcess.get(0);
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Drop Operation");
            confirmationAlert.setHeaderText("This action is irreversible!");
            confirmationAlert.setContentText("Are you sure you want to permanently drop the table '" + tableToDrop + "'?");
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) executeDropOperations(tablesToProcess);
                else outputArea.appendText("ℹ️ Drop operation cancelled by user.\n\n");
            });
        });
        mainContainer.getChildren().addAll(titleLabel, new Label("Select a table to permanently delete."), inputControls, executeBtn);
    }

    private void showSqlEditor() {
        clearMainContainer();
        Label titleLabel = new Label("SQL Editor");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #607D8B;");
        TextArea sqlInputArea = new TextArea();
        sqlInputArea.setPromptText("Enter SQL command(s) here...");
        VBox.setVgrow(sqlInputArea, Priority.ALWAYS);
        Button executeSqlBtn = new Button("Execute SQL");
        executeSqlBtn.setStyle("-fx-font-size: 14px; -fx-background-color: #607D8B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        executeSqlBtn.setMaxWidth(Double.MAX_VALUE);
        StackPane editorResultsContainer = new StackPane(new Label("Results will be shown here."));
        VBox.setVgrow(editorResultsContainer, Priority.ALWAYS);
        executeSqlBtn.setOnAction(e -> {
            String sql = sqlInputArea.getText().trim();
            if (sql.isEmpty()) { outputArea.appendText("❌ Error: SQL Editor is empty.\n\n"); return; }
            executeManualSql(sql, editorResultsContainer);
        });
        mainContainer.getChildren().addAll(titleLabel, sqlInputArea, executeSqlBtn, new Separator(), new Label("Results:"), editorResultsContainer);
    }

    private String getPrimaryKeyColumnName(String tableName) {
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            DatabaseMetaData metaData = con.getMetaData();
            try (ResultSet rs = metaData.getPrimaryKeys(null, null, tableName.toUpperCase())) {
                if (rs.next()) {
                    return rs.getString("COLUMN_NAME").toUpperCase();
                }
            }
        } catch (SQLException e) {
            outputArea.appendText("❌ DB Error getting PK for " + tableName + ": " + e.getMessage() + "\n");
        }
        return null;
    }

    private void executeDropOperations(List<String> tables) {
        try (Connection con = DriverManager.getConnection(url, user, pass); Statement stmt = con.createStatement()) {
            for (String tableName : tables) {
                stmt.executeUpdate("DROP TABLE " + tableName + " CASCADE CONSTRAINTS");
                outputArea.appendText("✅ Success! Table '" + tableName + "' was dropped.\n\n");
            }
        } catch (SQLException ex) {
            outputArea.appendText("❌ Database Error for table: " + ex.getMessage() + "\n\n");
        }
    }

    private List<String> getTablesToProcess(TextField manualField, ToggleGroup toggleGroup) {
        List<String> tables = new ArrayList<>();
        String manualTable = manualField.getText().trim();
        if (!manualTable.isEmpty()) {
            tables.add(manualTable.toUpperCase());
        } else {
            Toggle selectedToggle = toggleGroup.getSelectedToggle();
            if (selectedToggle instanceof RadioButton) tables.add(((RadioButton) selectedToggle).getText());
        }
        return tables;
    }

    private void processFileOperations(List<String> tables, String fileContent, String queryFileName) {
        for (String tableName : tables) {
            String queryBlock = findQueryBlockForTable(fileContent, tableName);
            if (queryBlock != null) executeStatements(queryBlock);
            else outputArea.appendText("❌ Error: No query block for '" + tableName + "' in " + queryFileName + ".\n\n");
        }
    }

    private void executeManualSql(String sql, StackPane resultsContainer) {
        resultsContainer.getChildren().clear();
        try (Connection con = DriverManager.getConnection(url, user, pass); Statement stmt = con.createStatement()) {
            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    resultsContainer.getChildren().add(createStaticTableViewFromResultSet(rs));
                }
            } else {
                int affectedRows = stmt.executeUpdate(sql);
                resultsContainer.getChildren().add(new Label("Command successful. " + affectedRows + " row(s) affected."));
            }
        } catch (SQLException ex) {
            resultsContainer.getChildren().add(new Label("❌ Database Error: " + ex.getMessage()));
        }
    }

    private TableView<Map<String, Object>> createStaticTableViewFromResultSet(ResultSet rs) throws SQLException {
        TableView<Map<String, Object>> resultTable = new TableView<>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            final String columnName = metaData.getColumnName(i).toUpperCase();
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(new MapValueFactory(columnName));
            resultTable.getColumns().add(column);
        }
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++)
                row.put(metaData.getColumnName(i).toUpperCase(), rs.getObject(i) != null ? rs.getObject(i).toString() : "NULL");
            resultTable.getItems().add(row);
        }
        return resultTable;
    }

    private String findFirstStatement(String queryBlock) {
        if (queryBlock == null || queryBlock.isEmpty()) return "";
        return queryBlock.split(";")[0].trim();
    }

    private String findQueryBlockForTable(String fileContent, String tableName) {
        String[] lines = fileContent.replaceAll("\r\n", "\n").split("\n");
        StringBuilder queryBlock = new StringBuilder();
        boolean inBlock = false;
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.toUpperCase().startsWith(TABLE_TAG_MARKER.toUpperCase())) {
                String extractedTable = trimmedLine.substring(TABLE_TAG_MARKER.length()).trim();
                inBlock = extractedTable.equalsIgnoreCase(tableName);
                continue;
            }
            if (inBlock) {
                queryBlock.append(line).append("\n");
            }
        }
        return queryBlock.length() > 0 ? queryBlock.toString().trim() : null;
    }

    private void executeStatements(String queryBlock) {
        try (Connection con = DriverManager.getConnection(url, user, pass); Statement stmt = con.createStatement()) {
            String[] statements = queryBlock.split(";");
            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    stmt.executeUpdate(sql.trim());
                }
            }
            outputArea.appendText("✅ Success! All statements in the block were executed.\n\n");
        } catch (SQLException ex) {
            outputArea.appendText("❌ Transaction Failed: " + ex.getMessage() + "\n\n");
        }
    }

    private String readQueryFromFile(String fileName) {
        try {
            return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            outputArea.appendText("❌ File Error: Could not read '" + fileName + "'.\n");
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}