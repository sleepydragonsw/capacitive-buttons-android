/*
 * This file is part of Capacitive Buttons.
 *
 * Capacitive Buttons is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Capacitive Buttons is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Capacitive Buttons.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sleepydragon.capbutnbrightness;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * The AppWidgetProvider for the application's widget.
 */
public class ButtonBrightnessAppWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        // NextBrightnessLevel is broadcasted when the widget is clicked
        if ("NextBrightnessLevel".equals(intent.getAction())) {
            setNextBrightnessLevel(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final RemoteViews views =
            new RemoteViews(context.getPackageName(),
                R.layout.brightness_appwidget);

        for (final int appWidgetId : appWidgetIds) {
            // set action to be performed when widget is clicked
            final Intent intent = new Intent();
            intent.setAction("NextBrightnessLevel");
            intent.setClass(context, this.getClass());
            intent.putExtra("AppWidgetId", appWidgetId);
            final PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
            views.setOnClickPendingIntent(R.id.widgetFlipper, pendingIntent);

            // set the initial image to display on the widget
            refreshWidgetImages(views, context);

            // update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private static SetBrightnessService.Level getNextBrightnessLevel(
            Context context) {
        // get the current brightness setting and calculate the "next" level
        final Settings settings = new Settings(context);
        final Integer level = settings.getLevel();

        if (level == null) {
            return SetBrightnessService.Level.BRIGHT;
        } else if (level == 0) {
            return SetBrightnessService.Level.DIM;
        } else if (level == 100) {
            return SetBrightnessService.Level.OFF;
        } else {
            return SetBrightnessService.Level.BRIGHT;
        }
    }

    /**
     * Broadcasts a notification to all widgets to update their state to reflect
     * the current brightness setting.
     *
     * @param context the context object to use.
     */
    public static void postUpdateWidgets(Context context) {
        final AppWidgetManager appWidgetManager =
            AppWidgetManager.getInstance(context);
        final int[] appWidgetIds =
            appWidgetManager.getAppWidgetIds(new ComponentName(context,
                ButtonBrightnessAppWidgetProvider.class));
        for (final int appWidgetId : appWidgetIds) {
            final RemoteViews views =
                new RemoteViews(context.getPackageName(),
                    R.layout.brightness_appwidget);
            refreshWidgetImages(views, context);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private static void refreshWidgetImages(RemoteViews views, Context context) {
        // set the displayed image to match the current brightness
        final Settings settings = new Settings(context);
        final Integer level = settings.getLevel();
        int displayedChildIndex;
        if (level == null) {
            displayedChildIndex = 0;
        } else if (level == 0) {
            displayedChildIndex = 0;
        } else if (level == 100) {
            displayedChildIndex = 2;
        } else {
            displayedChildIndex = 1;
        }
        views.setDisplayedChild(R.id.widgetFlipper, displayedChildIndex);
    }

    private static void setNextBrightnessLevel(Context context) {
        final SetBrightnessService.Level newLevel =
            getNextBrightnessLevel(context);
        SetBrightnessService.queueButtonBacklightBrightnessChange(newLevel, 0,
            true, context, null);
    }
}
