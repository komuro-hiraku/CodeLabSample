package kotlin.android.classmethod.jp.androidpay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.ActionBarActivity
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wallet.*

import com.google.android.gms.wallet.fragment.*


/**
 *
 *
 * Card Type: Visa<br>
 * Card Number: 4111 1111 1111 1111<br>
 * CVC: any three digits<br>
 * Expiration: any date in the future<br>
 *
 */
public class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {



    // Masked Wallet
    private var walletFragment: SupportWalletFragment? = null
    private var maskedWallet: MaskedWallet? = null


    private var googleApiClient: GoogleApiClient? = null
    private var fullWallet: FullWallet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        walletFragment = getSupportFragmentManager().findFragmentByTag(WALLET_FRAGMENT_ID) as SupportWalletFragment?

        if(walletFragment == null) {

            // Wallet Fragment Style
            val walletFragmentStyle = WalletFragmentStyle()
                    .setBuyButtonText(BuyButtonText.BUY_NOW)
                    .setBuyButtonWidth(Dimension.MATCH_PARENT)

            // Wallet Fragment Options
            val walletFragmentOptions = WalletFragmentOptions.newBuilder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                    .setFragmentStyle(walletFragmentStyle)
                    .setTheme(WalletConstants.THEME_LIGHT)
                    .setMode(WalletFragmentMode.BUY_BUTTON)
                    .build()

            // Wallet Fragmentを作成
            walletFragment = SupportWalletFragment.newInstance(walletFragmentOptions)
            val startParamsBuilder = WalletFragmentInitParams.newBuilder()
                    .setMaskedWalletRequest(generateMaskedWalletRequest())
                    .setMaskedWalletRequestCode(MASKED_WALLET_REQUEST_CODE)
                    .setAccountName("Google I/O Codelab")

            // Wallet Fragmentを初期化
            walletFragment?.initialize(startParamsBuilder.build())

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.wallet_button_holder, walletFragment, WALLET_FRAGMENT_ID).commit()
        }

        googleApiClient = GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(Wallet.API, Wallet.WalletOptions.Builder()
                                    .setEnvironment(WalletConstants.ENVIRONMENT_SANDBOX)
                                    .setTheme(WalletConstants.THEME_LIGHT).build())
                            .build()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    override fun onConnectionSuspended(cause: Int) {
    }

    override fun onConnected(bundle: Bundle?) {
    }

    override fun onConnectionFailed(result: ConnectionResult?) {
    }

    override fun onStart() {
        googleApiClient?.connect()
        super<AppCompatActivity>.onStart()
    }

    override fun onStop() {
        googleApiClient?.disconnect()
        super<AppCompatActivity>.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super<AppCompatActivity>.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            MASKED_WALLET_REQUEST_CODE -> {
                when(resultCode) {
                    Activity.RESULT_OK -> {
                        maskedWallet = data?.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET)
                        Toast.makeText(this@MainActivity, "ますくど財布をてにいれた", Toast.LENGTH_SHORT).show()
                    }

                    Activity.RESULT_CANCELED -> {
                        Toast.makeText(this@MainActivity, "きゃんせるだ！", Toast.LENGTH_SHORT).show()
                    }

                    WalletConstants.RESULT_ERROR -> {
                        Toast.makeText(this@MainActivity, "しっぱいしました", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            FULL_WALLET_REQUEST_CODE -> {
                when(resultCode) {
                    Activity.RESULT_OK -> {
                        fullWallet = data?.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET)
                        fullWallet?.let {
                            Toast.makeText(this, it.getProxyCard().getPan().toString(), Toast.LENGTH_SHORT).show()       // クレジットカード番号表示
                        }
                    }

                    WalletConstants.RESULT_ERROR -> {
                        Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    public fun requestFullWallet(view: View) {
        maskedWallet?.let {
            Wallet.Payments.loadFullWallet(googleApiClient,
                    generateFullWalletRequest(it.getGoogleTransactionId()),
                    FULL_WALLET_REQUEST_CODE)
        }
    }


    private fun generateMaskedWalletRequest(): MaskedWalletRequest? {
        val maskedWalletRequest = MaskedWalletRequest.newBuilder()
        .setMerchantName("Google I/O Codelab")
        .setPhoneNumberRequired(true)
        .setShippingAddressRequired(true)
        .setCurrencyCode("USD")
        .setCart(Cart.newBuilder()
                    .setCurrencyCode("USD")
                    .setTotalPrice("10.00")
                    .addLineItem(LineItem.newBuilder()
                        .setCurrencyCode("USD")
                        .setDescription("Google I/O Sticker")
                        .setQuantity("1")
                        .setUnitPrice("10.00")
                        .setTotalPrice("10.00")
                            .build()
                    ).build()
        ).setEstimatedTotalPrice("15.00").build()
        return maskedWalletRequest
    }


    private fun generateFullWalletRequest(googleTransactionId:String): FullWalletRequest {
        val fullWalletRequest = FullWalletRequest.newBuilder()
                .setGoogleTransactionId(googleTransactionId) // Transaction Id?
                .setCart(Cart.newBuilder()
                        .setCurrencyCode("USD")
                        .setTotalPrice("10.10")
                        .addLineItem(LineItem.newBuilder()  // ステッカーを1アイテム作成
                                .setCurrencyCode("USD")
                                .setDescription("Google I/O Sticker")
                                .setQuantity("1")
                                .setUnitPrice("10.00")
                                .setTotalPrice("10.00")
                                .build())
                        .addLineItem(LineItem.newBuilder()  // 税金を1アイテムとして作成
                                .setCurrencyCode("USD")
                                .setDescription("Tax")
                                .setRole(LineItem.Role.TAX) // Roleを税金に設定
                                .setTotalPrice(".10")
                                .build())
                        .build())
                .build();
        return fullWalletRequest
    }

    companion object {
        val MASKED_WALLET_REQUEST_CODE = 888
        val WALLET_FRAGMENT_ID = "wallet_fragment"

        val FULL_WALLET_REQUEST_CODE = 889
    }
}
