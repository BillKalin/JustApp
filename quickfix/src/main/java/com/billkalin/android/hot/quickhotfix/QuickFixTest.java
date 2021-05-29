/*
package com.billkalin.android.hot.quickhotfix;

 class SuperClass {
     public void aaa() {

        }
}

class QuickFixTest extends SuperClass {

    public static QuickHotFix mChange;

    public String show(String p1, int p2, char[] p3, Object obj) {
        QuickHotFix change = mChange;
        if (change != null) {
            return (String) change.dispatch("show", new Object[]{p1, p2, p3, obj});
        }
        String str = "dww";
        Runnable aa = new Runnable() {
            @Override
            public void run() {
                if (mChange != null) {
                    mChange.dispatch("show", new Object[0]);
                }
            }
        };

        return str;
    }

}

*/
