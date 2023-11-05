package com.wq.wfEngine.pojo;

import java.io.Serializable;

public class VipChannel implements Serializable {
    /*
     * 表明公司方对用户提出的问题是否可以解决的状态
     */
    private boolean solve;
    /*
     * 表示用户对公司给出的解决方案的认可状态
     */
    private boolean havesolve;
    /*
     * 用户提出的问题
     */
    private String question;
    /*
     * 公司给出的解决方案
     */
    private String solution;

    public boolean isSolve() {
        return solve;
    }

    public void setSolve(boolean solve) {
        this.solve = solve;
    }

    public boolean isHavesolve() {
        return havesolve;
    }

    public void setHavesolve(boolean havesolve) {
        this.havesolve = havesolve;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}