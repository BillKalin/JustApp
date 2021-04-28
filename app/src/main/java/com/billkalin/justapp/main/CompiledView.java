package com.billkalin.justapp.main;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class CompiledView {

static <T extends View> T createView(Context context, AttributeSet attrs, View parent, String name, LayoutInflater.Factory factory, LayoutInflater.Factory2 factory2) {
  if (factory2 != null) {
    return (T)factory2.onCreateView(parent, name, context, attrs);
  } else if (factory != null) {
    return (T)factory.onCreateView(name, context, attrs);
  }
  return null;
}

  public static View inflate(Context context, int layoutId) {
    try {
      LayoutInflater inflater = LayoutInflater.from(context);
      LayoutInflater.Factory factory = inflater.getFactory();
      LayoutInflater.Factory2 factory2 = inflater.getFactory2();
      Resources res = context.getResources();
      XmlResourceParser xml = res.getLayout(layoutId);
      AttributeSet attrs = Xml.asAttributeSet(xml);
      xml.next(); // start document
      xml.next(); // <androidx.core.widget.NestedScrollView>
      androidx.core.widget.NestedScrollView view0 = createView(context, attrs, null, "androidx.core.widget.NestedScrollView", factory, factory2);
      if (view0 == null) view0 = new androidx.core.widget.NestedScrollView(context, attrs);
      xml.next(); // <LinearLayout>
      LinearLayout view2 = createView(context, attrs, view0, "LinearLayout", factory, factory2);
      if (view2 == null) view2 = new LinearLayout(context, attrs);
      ViewGroup.LayoutParams layout3 = view0.generateLayoutParams(attrs);
      xml.next(); // <Button>
      Button view4 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view4 == null) view4 = new Button(context, attrs);
      ViewGroup.LayoutParams layout5 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view4, layout5);
      xml.next(); // <Button>
      Button view6 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view6 == null) view6 = new Button(context, attrs);
      ViewGroup.LayoutParams layout7 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view6, layout7);
      xml.next(); // <Button>
      Button view8 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view8 == null) view8 = new Button(context, attrs);
      ViewGroup.LayoutParams layout9 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view8, layout9);
      xml.next(); // <Button>
      Button view10 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view10 == null) view10 = new Button(context, attrs);
      ViewGroup.LayoutParams layout11 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view10, layout11);
      xml.next(); // <LinearLayout>
      LinearLayout view12 = createView(context, attrs, view2, "LinearLayout", factory, factory2);
      if (view12 == null) view12 = new LinearLayout(context, attrs);
      ViewGroup.LayoutParams layout13 = view2.generateLayoutParams(attrs);
      xml.next(); // <com.billkalin.justapp.main.CustomButton>
      CustomButton view14 = createView(context, attrs, view12, "com.billkalin.justapp.main.CustomButton", factory, factory2);
      if (view14 == null) view14 = new CustomButton(context, attrs);
      ViewGroup.LayoutParams layout15 = view12.generateLayoutParams(attrs);
      xml.next(); // </com.billkalin.justapp.main.CustomButton>
      view12.addView(view14, layout15);
      xml.next(); // <com.billkalin.justapp.main.CustomButton>
      CustomButton view16 = createView(context, attrs, view12, "com.billkalin.justapp.main.CustomButton", factory, factory2);
      if (view16 == null) view16 = new CustomButton(context, attrs);
      ViewGroup.LayoutParams layout17 = view12.generateLayoutParams(attrs);
      xml.next(); // </com.billkalin.justapp.main.CustomButton>
      view12.addView(view16, layout17);
      xml.next(); // </LinearLayout>
      view2.addView(view12, layout13);
      xml.next(); // <com.billkalin.justapp.main.CustomButton>
      CustomButton view18 = createView(context, attrs, view2, "com.billkalin.justapp.main.CustomButton", factory, factory2);
      if (view18 == null) view18 = new CustomButton(context, attrs);
      ViewGroup.LayoutParams layout19 = view2.generateLayoutParams(attrs);
      xml.next(); // </com.billkalin.justapp.main.CustomButton>
      view2.addView(view18, layout19);
      xml.next(); // <LinearLayout>
      LinearLayout view20 = createView(context, attrs, view2, "LinearLayout", factory, factory2);
      if (view20 == null) view20 = new LinearLayout(context, attrs);
      ViewGroup.LayoutParams layout21 = view2.generateLayoutParams(attrs);
      xml.next(); // <Button>
      Button view22 = createView(context, attrs, view20, "Button", factory, factory2);
      if (view22 == null) view22 = new Button(context, attrs);
      ViewGroup.LayoutParams layout23 = view20.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view20.addView(view22, layout23);
      xml.next(); // <Button>
      Button view24 = createView(context, attrs, view20, "Button", factory, factory2);
      if (view24 == null) view24 = new Button(context, attrs);
      ViewGroup.LayoutParams layout25 = view20.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view20.addView(view24, layout25);
      xml.next(); // <Button>
      Button view26 = createView(context, attrs, view20, "Button", factory, factory2);
      if (view26 == null) view26 = new Button(context, attrs);
      ViewGroup.LayoutParams layout27 = view20.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view20.addView(view26, layout27);
      xml.next(); // </LinearLayout>
      view2.addView(view20, layout21);
      xml.next(); // <Button>
      Button view28 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view28 == null) view28 = new Button(context, attrs);
      ViewGroup.LayoutParams layout29 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view28, layout29);
      xml.next(); // <Button>
      Button view30 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view30 == null) view30 = new Button(context, attrs);
      ViewGroup.LayoutParams layout31 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view30, layout31);
      xml.next(); // <Button>
      Button view32 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view32 == null) view32 = new Button(context, attrs);
      ViewGroup.LayoutParams layout33 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view32, layout33);
      xml.next(); // <Button>
      Button view34 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view34 == null) view34 = new Button(context, attrs);
      ViewGroup.LayoutParams layout35 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view34, layout35);
      xml.next(); // <Button>
      Button view36 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view36 == null) view36 = new Button(context, attrs);
      ViewGroup.LayoutParams layout37 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view36, layout37);
      xml.next(); // <Button>
      Button view38 = createView(context, attrs, view2, "Button", factory, factory2);
      if (view38 == null) view38 = new Button(context, attrs);
      ViewGroup.LayoutParams layout39 = view2.generateLayoutParams(attrs);
      xml.next(); // </Button>
      view2.addView(view38, layout39);
      xml.next(); // </LinearLayout>
      view0.addView(view2, layout3);
      return view0;
    } catch (Exception e) {
      return null;
    }
  }
}
