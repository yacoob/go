package org.yacoob.trampoline;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * {@link ArrayAdapter} containing {@link UrlEntry} objects.
 */
@Deprecated
class HopListAdapter extends ArrayAdapter<UrlEntry> {

    /**
     * Helper class, used for *drumroll* Viewholder pattern (@see <a
     * href="http://bit.ly/lg8AJ0">thread</a>). It's used to save time on
     * (supposedly) costly findViewById calls. Instead of making them every time
     * we inflate/change view in getView(), we perform them only during view
     * creation, save refs of child views in viewholder, and put viewholder in
     * view's tag.
     * 
     * This class has as many members as child views in view used by
     * {@link HopListAdapter}.
     */
    private class ViewHolder {

        /** TextView holding first line of text (URL). */
        protected TextView first;

        /** TextView holding second line of text (timestamp). */
        protected TextView second;
    }

    /** {@link LayoutInflater} used during views instantiation. */
    private LayoutInflater li;

    /**
     * {@link HopListAdapter} constructor.
     * 
     * @param context
     *            current context
     * @param objects
     *            {@link List} of {@link UrlEntry} to be placed in the list.
     */
    HopListAdapter(final Context context, final List<UrlEntry> objects) {
        super(context, R.layout.listitem, objects);
        li = LayoutInflater.from(context);
    }

    /**
     * Reconstructs the {@link List} of currently present items.
     * 
     * @return current {@link List} of items on the list.
     */
    public List<UrlEntry> getUrlList() {
        List<UrlEntry> list = new ArrayList<UrlEntry>();
        for (int i = 0; i < getCount(); i++) {
            list.add(getItem(i));
        }
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.ArrayAdapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {

        ViewHolder holder;
        View tmp = convertView;

        // Are we recycling a view?
        if (tmp == null) {
            tmp = li.inflate(R.layout.listitem, parent, false);
            holder = new ViewHolder();
            // Save references to child views in holder.
            holder.first = (TextView) tmp.findViewById(R.id.first);
            holder.second = (TextView) tmp.findViewById(R.id.second);
            // Stash holder in tag.
            tmp.setTag(holder);
        } else {
            // It's a recycled view; get the holder.
            holder = (ViewHolder) tmp.getTag();
        }
        UrlEntry item = getItem(position);
        // Set labels using references stored in holder.
        holder.first.setText(item.getDisplayUrl());
        holder.second.setText(item.getDate());
        return tmp;
    }
}