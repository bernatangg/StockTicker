package com.github.premnirmal.ticker.news

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.premnirmal.ticker.base.BaseGraphActivity
import com.github.premnirmal.ticker.components.Injector
import com.github.premnirmal.ticker.isNetworkOnline
import com.github.premnirmal.ticker.model.IHistoryProvider
import com.github.premnirmal.ticker.model.IHistoryProvider.Range
import com.github.premnirmal.ticker.model.IStocksProvider
import com.github.premnirmal.ticker.network.data.Quote
import com.github.premnirmal.ticker.showDialog
import com.github.premnirmal.tickerwidget.R
import kotlinx.android.synthetic.main.activity_graph.desc
import kotlinx.android.synthetic.main.activity_graph.graph_holder
import kotlinx.android.synthetic.main.activity_graph.max
import kotlinx.android.synthetic.main.activity_graph.one_month
import kotlinx.android.synthetic.main.activity_graph.one_year
import kotlinx.android.synthetic.main.activity_graph.progress
import kotlinx.android.synthetic.main.activity_graph.three_month
import kotlinx.android.synthetic.main.activity_graph.tickerName
import kotlinx.coroutines.launch
import javax.inject.Inject

class GraphActivity : BaseGraphActivity() {

  companion object {
    const val TICKER = "TICKER"
    private const val DATA_POINTS = "DATA_POINTS"
    private const val RANGE = "RANGE"
    private const val DURATION = 2000
  }

  private var range = Range.THREE_MONTH
  private lateinit var ticker: String
  @Inject internal lateinit var historyProvider: IHistoryProvider
  @Inject internal lateinit var stocksProvider: IStocksProvider
  override val simpleName: String = "GraphActivity"

  override fun onCreate(savedInstanceState: Bundle?) {
    Injector.appComponent.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_graph)
    setupGraphView()
    val q: Quote?
    if (intent.hasExtra(TICKER) && intent.getStringExtra(TICKER) != null) {
      ticker = intent.getStringExtra(TICKER)
      q = stocksProvider.getStock(ticker)
      if (q == null) {
        showErrorAndFinish()
        return
      }
    } else {
      ticker = ""
      showErrorAndFinish()
      return
    }
    quote = q
    tickerName.text = ticker
    desc.text = quote.name

    savedInstanceState?.let {
      dataPoints = it.getParcelableArrayList(DATA_POINTS)
    }

    var view: View? = null
    when (range) {
      Range.ONE_MONTH -> view = one_month
      Range.THREE_MONTH -> view = three_month
      Range.ONE_YEAR -> view = one_year
      Range.MAX -> view = max
    }
    view?.isEnabled = false
  }

  override fun onStart() {
    super.onStart()
    if (dataPoints == null) {
      getData()
    } else {
      loadGraph()
    }
  }

  // Seeing if this fixes the parcelable too large exception
//  override fun onSaveInstanceState(outState: Bundle) {
//    super.onSaveInstanceState(outState)
//    dataPoints?.let {
//      outState.putParcelableArrayList(DATA_POINTS, ArrayList(it))
//    }
//    outState.putSerializable(RANGE, range)
//  }

  private fun getData() {
    if (isNetworkOnline()) {
      graph_holder.visibility = View.GONE
      progress.visibility = View.VISIBLE
      lifecycleScope.launch {
        val result = historyProvider.getHistoricalDataByRange(ticker, range)
        if (result.wasSuccessful) {
          dataPoints = result.data
          loadGraph()
        } else {
          showDialog(getString(R.string.error_loading_graph),
              DialogInterface.OnClickListener { _, _ -> finish() }, cancelable = false)
        }
      }
    } else {
      showDialog(getString(R.string.no_network_message),
          DialogInterface.OnClickListener { _, _ -> finish() }, cancelable = false)
    }
  }

  override fun onGraphDataAdded(graphView: LineChart) {
    progress.visibility = View.GONE
    graph_holder.visibility = View.VISIBLE
    graphView.animateX(DURATION, Easing.EasingOption.EaseInOutCubic)
  }

  override fun onNoGraphData(graphView: LineChart) {
    progress.visibility = View.GONE
    graph_holder.visibility = View.VISIBLE
  }

  /**
   * xml OnClick
   * @param v
   */
  fun updateRange(v: View) {
    when (v.id) {
      R.id.one_month -> range = Range.ONE_MONTH
      R.id.three_month -> range = Range.THREE_MONTH
      R.id.one_year -> range = Range.ONE_YEAR
      R.id.max -> range = Range.MAX
    }
    val parent = v.parent as ViewGroup
    (0 until parent.childCount).map { parent.getChildAt(it) }
        .forEach { it.isEnabled = it != v }
    getData()
  }
}