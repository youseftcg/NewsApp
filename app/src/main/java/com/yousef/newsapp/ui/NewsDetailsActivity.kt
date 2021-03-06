package com.yousef.newsapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.yousef.newsapp.R
import com.yousef.newsapp.database.AppDatabase
import com.yousef.newsapp.databinding.ActivityNewsDetailsBinding
import com.yousef.newsapp.models.Article
import com.yousef.newsapp.ui.viewmodels.NewsDetailsViewModel
import com.yousef.newsapp.ui.viewmodels.NewsDetailsViewModelFactory

class NewsDetailsActivity : AppCompatActivity() {
    companion object {
        const val NEWS_EXTRA = "newsExtra"
    }

    private var isFavorite = false
    private lateinit var viewModel:NewsDetailsViewModel

    lateinit var article: Article
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val binding: ActivityNewsDetailsBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_news_details)

        article = intent.getParcelableExtra(NEWS_EXTRA) ?: article

        binding.news = article
        binding.activity = this


        val database = AppDatabase.getInstance(application).newsDao()
        val viewModelFactory = NewsDetailsViewModelFactory(database)
        viewModel = ViewModelProvider(this, viewModelFactory).get(NewsDetailsViewModel::class.java)

        checkIfNewsIsFavorite(article.publishedAt)


        viewModel.markedAsFavorite.observe(this){ markedAsFavorite ->
            if(markedAsFavorite){
                markNewsAsFavorite()
            } else{
                unFavoriteNews()
            }

        }

    }


    private fun checkIfNewsIsFavorite(publishedAt: String) {
        viewModel.checkIfNewsIsFavorite(publishedAt)
    }

    private fun markNewsAsFavorite() {
        isFavorite = true
        invalidateOptionsMenu() // Re-Draw the action bar to change the favorite icon to the filled one
    }

    private fun unFavoriteNews() {
        isFavorite = false
        invalidateOptionsMenu() // Re-Draw the action bar to change the favorite icon to the filled one
    }

    fun openNewsSourceActivity(url: String) {
        val intent = Intent(this, NewsSourceActivity::class.java)
        intent.putExtra(NewsSourceActivity.NEWS_URL_EXTRA, url)
        startActivity(intent)
    }

    private fun removeNewsFromFavorites(article: Article) {
        viewModel.removeNewsFromFavorites(article)
    }


    private fun shareNews() {
        val checkOutNewsText = getString(R.string.check_out_this_news, article.url)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, checkOutNewsText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)

    }

    private fun addNewsToFavorites(article: Article) {
        viewModel.addNewsToFavorites(article)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val favMenuItem = menu?.findItem(R.id.add_to_favorite)

        // If the news is favorite, show the filled icon, otherwise show the empty icon
        if (isFavorite) {
            favMenuItem?.setIcon(R.drawable.ic_filled_favorite)
        } else {
            favMenuItem?.setIcon(R.drawable.ic_outline_favorite_border)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.news_details_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_to_favorite -> {
                if (isFavorite) {
                    removeNewsFromFavorites(article)
                } else {
                    addNewsToFavorites(article)
                }
                true
            }
            R.id.share -> {
                shareNews()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}