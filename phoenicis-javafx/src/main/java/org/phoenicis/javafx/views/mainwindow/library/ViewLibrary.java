/*
 * Copyright (C) 2015-2017 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.phoenicis.javafx.views.mainwindow.library;

import org.phoenicis.javafx.views.common.ThemeManager;
import org.phoenicis.library.dto.ShortcutDTO;
import org.phoenicis.javafx.views.common.widget.MiniatureListWidget;
import org.phoenicis.javafx.views.mainwindow.MainWindowView;
import org.phoenicis.javafx.views.mainwindow.ui.LeftBarTitle;
import org.phoenicis.javafx.views.mainwindow.ui.LeftButton;
import org.phoenicis.javafx.views.mainwindow.ui.LeftGroup;
import org.phoenicis.javafx.views.mainwindow.ui.LeftSpacer;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.phoenicis.configuration.localisation.Localisation.translate;

public class ViewLibrary extends MainWindowView {
    private final Logger LOGGER = LoggerFactory.getLogger(ViewLibrary.class);

    private LeftButton runScript;
    private LeftButton runConsole;
    private MiniatureListWidget applicationListWidget;
    private TextField searchBar;
    private TabPane libraryTabs;
    private Runnable onTabOpened = () -> {};
    private Consumer<ShortcutDTO> onShortcutSelected = shortcut -> {};
    private Consumer<ShortcutDTO> onShortcutDoubleClicked = shortcut -> {};

    private Consumer<ShortcutDTO> onShortcutRun = shortcut -> {};
    private Consumer<ShortcutDTO> onShortcutUninstall = shortcutDTO -> {};

    private Consumer<ShortcutDTO> onShortcutStop = shortcutDTO -> {};
    private Consumer<File> onScriptRun = script -> {};

    private String lastSearch = "";

    private Consumer<String> onSearch = keyword -> {};

    public ViewLibrary(String applicationName, ThemeManager themeManager) {
        super("Library", themeManager);
        this.getStyleClass().add("mainWindowScene");
        this.runScript = new LeftButton(translate("Run a script"));
        final String scriptButtonIcon = "icons/mainwindow/library/script.png";
        this.runScript.setStyle("-fx-background-image: url('" + themeManager.getResourceUrl(scriptButtonIcon) + "');");
        this.runConsole = new LeftButton(translate(applicationName + " console"));
        final String consoleButtonIcon = "icons/mainwindow/library/console.png";
        this.runConsole.setStyle("-fx-background-image: url('" + themeManager.getResourceUrl(consoleButtonIcon) + "');");

        this.drawSideBar();
        this.drawContent();

        showRightView(libraryTabs);
    }

    public void setOnShortcutSelected(Consumer<ShortcutDTO> onShortcutSelected) {
        this.onShortcutSelected = onShortcutSelected;
    }

    public void setOnShortcutDoubleClicked(Consumer<ShortcutDTO> onShortcutDoubleClicked) {
        this.onShortcutDoubleClicked = onShortcutDoubleClicked;
    }

    public void setOnShortcutStop(Consumer<ShortcutDTO> onShortcutStop) {
        this.onShortcutStop = onShortcutStop;
    }

    public void setOnSearch(Consumer<String> onSearch) {
        this.onSearch = onSearch;
    }

    public void setOnShortcutRun(Consumer<ShortcutDTO> onShortcutRun) {
        this.onShortcutRun = onShortcutRun;
    }

    public void populate(List<ShortcutDTO> shortcutDTOs) {
        applicationListWidget.clear();
        for (ShortcutDTO shortcutDTO : shortcutDTOs) {
            final MiniatureListWidget.Element selectedItem
                    = applicationListWidget.addItem(shortcutDTO.getName(), shortcutDTO.getMiniature());

            selectedItem.setOnMouseClicked(event -> {
                applicationListWidget.unSelecteAll();
                applicationListWidget.select(selectedItem);
                onShortcutSelected.accept(shortcutDTO);

                clearSideBar();
                drawSideBarWithShortcut(shortcutDTO);

                if(event.getClickCount() == 2) {
                    onShortcutDoubleClicked.accept(shortcutDTO);
                }

                event.consume();
            });
        }

        applicationListWidget.setOnMouseClicked(event -> {
            clearSideBar();
            drawSideBarWithoutShortcut();
            applicationListWidget.unSelecteAll();
            onShortcutSelected.accept(null);
            event.consume();
        });
    }

    private void drawContent() {
        libraryTabs = new TabPane();
        libraryTabs.getStyleClass().add("rightPane");

        final Tab installedApplication = new Tab();
        installedApplication.setClosable(false);
        installedApplication.setText(translate("My applications"));
        libraryTabs.getTabs().add(installedApplication);

        applicationListWidget = MiniatureListWidget.create();

        installedApplication.setContent(applicationListWidget);
    }

    @Override
    protected void drawSideBar() {
        drawSideBarWithoutShortcut();
        super.drawSideBar();
    }

    private void drawSideBarWithShortcut(ShortcutDTO shortcut) {
        searchBar = new TextField();
        searchBar.getStyleClass().add("searchBar");
        searchBar.setOnKeyReleased(event -> applyFilter(searchBar.getText()));

        addToSideBar(searchBar, new LeftSpacer(), shortcutGroup(shortcut), new LeftSpacer(), new LeftBarTitle("Advanced tools"), runScript, runConsole);
    }

    private LeftGroup shortcutGroup(ShortcutDTO shortcut) {
        final LeftGroup shortcutGroup = new LeftGroup(shortcut.getName());
        final LeftButton runButton = new LeftButton(translate("Run"));
        final String runButtonIcon = "icons/mainwindow/library/play.png";
        runButton.setStyle("-fx-background-image: url('" + themeManager.getResourceUrl(runButtonIcon) + "');");
        final LeftButton stopButton = new LeftButton(translate("Close"));
        final String stopButtonIcon = "icons/mainwindow/library/stop.png";
        stopButton.setStyle("-fx-background-image: url('" + themeManager.getResourceUrl(stopButtonIcon) + "');");
        final LeftButton uninstallButton = new LeftButton(translate("Uninstall"));
        final String uninstallButtonIcon = "icons/mainwindow/library/remove.png";
        uninstallButton.setStyle("-fx-background-image: url('" + themeManager.getResourceUrl(uninstallButtonIcon) + "');");

        runButton.setOnMouseClicked(event -> onShortcutRun.accept(shortcut));
        uninstallButton.setOnMouseClicked(event -> {onShortcutUninstall.accept(shortcut); drawSideBarWithoutShortcut();});
        stopButton.setOnMouseClicked(event -> onShortcutStop.accept(shortcut));

        shortcutGroup.setNodes(Arrays.asList(
                runButton,
                stopButton,
                uninstallButton
        ));
        return shortcutGroup;
    }

    private void drawSideBarWithoutShortcut() {
        clearSideBar();

        searchBar = new TextField();
        searchBar.getStyleClass().add("searchBar");
        searchBar.setOnKeyReleased(event -> applyFilter(searchBar.getText()));

        addToSideBar(searchBar, new LeftSpacer(), new LeftBarTitle("Advanced tools"), runScript, runConsole);
    }

    private void applyFilter(String searchText) {
        if (!lastSearch.equals(searchText)) {
            this.onSearch.accept(searchText);
            lastSearch = searchText;
        }
    }

    public void setUpEvents() {
        runScript.setOnMouseClicked(event -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open a script");
            final File scriptToRun = fileChooser.showOpenDialog(null);

            if (scriptToRun != null) {
                onScriptRun.accept(scriptToRun);
            }
        });

    }

    public void createNewTab(Tab tab) {
        libraryTabs.getTabs().add(tab);
        libraryTabs.getSelectionModel().select(tab);
        onTabOpened.run();
    }

    public void closeTab(Tab tab) {
        libraryTabs.getTabs().remove(tab);
    }

    public void setOnTabOpened(Runnable onTabOpened) {
        this.onTabOpened = onTabOpened;
    }

    public void setOnOpenConsole(Runnable onOpenConsole) {
        runConsole.setOnMouseClicked(event -> onOpenConsole.run());
    }

    public void setOnShortcutUninstall(Consumer<ShortcutDTO> onShortcutUninstall) {
        this.onShortcutUninstall = onShortcutUninstall;
    }

    public void setOnScriptRun(Consumer<File> onScriptRun) {
        this.onScriptRun = onScriptRun;
    }
}
