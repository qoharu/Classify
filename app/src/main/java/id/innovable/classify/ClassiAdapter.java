package id.innovable.classify;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Qoharu on 4/11/17.
 */

public class ClassiAdapter extends RecyclerView.Adapter<ClassiAdapter.ViewHolder> {
    String[] SubjectValues;
    List<Classification> contents;
    Context context;
    View view1;
    ViewHolder viewHolder1;


    public ClassiAdapter(Context context1,List<Classification> contents1){

        contents = contents1;
        context = context1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textView;
        public ViewHolder(View v){
            super(v);
            textView = (TextView)v.findViewById(R.id.info_text);
        }
    }

    @Override
    public ClassiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        view1 = LayoutInflater.from(context).inflate(R.layout.list_content,parent,false);
        viewHolder1 = new ViewHolder(view1);
        return viewHolder1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        String classname = contents.get(position).getClassName();
        String percent = contents.get(position).getPercentage().toString();
        String textcontent = classname+" "+percent;

        holder.textView.setText(textcontent);
    }

    @Override
    public int getItemCount(){
        return this.contents.size();
    }
}
