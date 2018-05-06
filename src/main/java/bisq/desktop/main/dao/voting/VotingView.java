/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.main.dao.voting;

import bisq.desktop.Navigation;
import bisq.desktop.common.view.ActivatableViewAndModel;
import bisq.desktop.common.view.CachingViewLoader;
import bisq.desktop.common.view.FxmlView;
import bisq.desktop.common.view.View;
import bisq.desktop.common.view.ViewLoader;
import bisq.desktop.common.view.ViewPath;
import bisq.desktop.components.MenuItem;
import bisq.desktop.main.MainView;
import bisq.desktop.main.dao.DaoView;
import bisq.desktop.main.dao.voting.active.ActiveBallotsView;
import bisq.desktop.main.dao.voting.closed.ClosedBallotsView;
import bisq.desktop.main.dao.voting.dashboard.VotingDashboardView;

import bisq.core.locale.Res;

import javax.inject.Inject;

import de.jensd.fx.fontawesome.AwesomeIcon;

import javafx.fxml.FXML;

import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;

@FxmlView
public class VotingView extends ActivatableViewAndModel {

    private final ViewLoader viewLoader;
    private final Navigation navigation;

    private MenuItem dashboard, activeBallots, closedBallots;
    private Navigation.Listener listener;

    @FXML
    private VBox leftVBox;
    @FXML
    private AnchorPane content;

    private Class<? extends View> selectedViewClass;

    @Inject
    private VotingView(CachingViewLoader viewLoader, Navigation navigation) {
        this.viewLoader = viewLoader;
        this.navigation = navigation;
    }

    @Override
    public void initialize() {
        listener = viewPath -> {
            if (viewPath.size() != 4 || viewPath.indexOf(VotingView.class) != 2)
                return;

            selectedViewClass = viewPath.tip();
            loadView(selectedViewClass);
        };

        ToggleGroup toggleGroup = new ToggleGroup();
        final List<Class<? extends View>> baseNavPath = Arrays.asList(MainView.class, DaoView.class, VotingView.class);
        dashboard = new MenuItem(navigation, toggleGroup, Res.get("shared.dashboard"),
                VotingDashboardView.class, AwesomeIcon.DASHBOARD, baseNavPath);
        activeBallots = new MenuItem(navigation, toggleGroup, Res.get("dao.voting.menuItem.activeBallots"),
                ActiveBallotsView.class, AwesomeIcon.LIST_UL, baseNavPath);
        closedBallots = new MenuItem(navigation, toggleGroup, Res.get("dao.voting.menuItem.closedBallots"),
                ClosedBallotsView.class, AwesomeIcon.LIST_UL, baseNavPath);
        leftVBox.getChildren().addAll(dashboard, activeBallots, closedBallots);
    }

    @Override
    protected void activate() {
        dashboard.activate();
        activeBallots.activate();
        closedBallots.activate();

        navigation.addListener(listener);
        ViewPath viewPath = navigation.getCurrentPath();
        if (viewPath.size() == 3 && viewPath.indexOf(VotingView.class) == 2 ||
                viewPath.size() == 2 && viewPath.indexOf(DaoView.class) == 1) {
            if (selectedViewClass == null)
                selectedViewClass = ActiveBallotsView.class;

            loadView(selectedViewClass);

        } else if (viewPath.size() == 4 && viewPath.indexOf(VotingView.class) == 2) {
            selectedViewClass = viewPath.get(3);
            loadView(selectedViewClass);
        }
    }

    @Override
    protected void deactivate() {
        navigation.removeListener(listener);

        dashboard.deactivate();
        activeBallots.deactivate();
        closedBallots.deactivate();
    }

    private void loadView(Class<? extends View> viewClass) {
        View view = viewLoader.load(viewClass);
        content.getChildren().setAll(view.getRoot());

        if (view instanceof VotingDashboardView) dashboard.setSelected(true);
        else if (view instanceof ActiveBallotsView) activeBallots.setSelected(true);
        else if (view instanceof ClosedBallotsView) closedBallots.setSelected(true);
    }

    public Class<? extends View> getSelectedViewClass() {
        return selectedViewClass;
    }
}
