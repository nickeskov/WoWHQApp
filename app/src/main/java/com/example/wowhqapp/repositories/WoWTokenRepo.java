package com.example.wowhqapp.repositories;

import android.util.Log;

import com.example.wowhqapp.contracts.MainContract;
import com.example.wowhqapp.databases.dao.WoWTokenDao;
import com.example.wowhqapp.databases.database.AucAndTokenDataBase;
import com.example.wowhqapp.databases.entity.WoWToken;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;


public class WoWTokenRepo implements MainContract.TokenRepository {

    private String mRegion;

    private WoWTokenDao mWoWTokenDao;
    private WoWToken mWoWToken;
    private CompositeDisposable mCompositeDisposable; //Будет хранить все подписки, чтобы затем мы могли вызвать .clear в нужный момент (onDestroy) и избежать утечки памяти.

    private MainContract.WoWTokenPresenter mWoWTokenPresenter;

    public WoWTokenRepo(String region, MainContract.WoWTokenPresenter woWTokenPresenter){
        mWoWTokenPresenter = woWTokenPresenter;
        mRegion =region;
        mWoWTokenDao = AucAndTokenDataBase.getDB().getWoWTokenDao();
        mCompositeDisposable = new CompositeDisposable();

        mCompositeDisposable.add(
                mWoWTokenDao.getByRegion(mRegion).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<WoWToken>>() {
                    @Override
                    public void accept(List<WoWToken> woWToken) throws Exception {
                        Log.v("REPO_TOKEN", "accept сработал");
                        if (woWToken.isEmpty()){mWoWToken = null; Log.v("REPO_TOKEN", "Получили нулевой токен");}
                        else {
                            mWoWToken = woWToken.get(0);
                            mWoWTokenPresenter.init_price();
                            Log.v("REPO_TOKEN", "Получили НЕ нулевой токен");
                        }
                    }
                })
        );
    }

    @Override
    public void refreshMinMaxCurrent() {

    }

    @Override
    public void test_insertToken(int coef) {
        long num = 112*coef;
        WoWToken woWToken = new WoWToken("eu", "2019", num,num,num,num);
        mWoWTokenDao.insert(woWToken);
        Log.v("REPO_TOKEN", "Insert");
    }

    @Override
    public long getMax() {
        return mWoWToken.getOneDayHigh();
    }

    @Override
    public long getMin() {
        Log.v("REPO_TOKEN", "Вызван GetMin");
        return mWoWToken.getOneDayLow();
    }

    @Override
    public long getCurrent() {
        return mWoWToken.getCurrentPriceBlizzardApi();
    }

    @Override
    public long getlastChange() {
        return mWoWToken.getLastChange();
    }

    @Override
    public void destroy() {
        mCompositeDisposable.clear();
    }
}
