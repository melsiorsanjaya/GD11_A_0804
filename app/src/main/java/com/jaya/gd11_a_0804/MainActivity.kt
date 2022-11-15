package com.jaya.gd11_a_0804

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jaya.gd11_a_0804.adapters.MahasiswaAdapter
import com.jaya.gd11_a_0804.api.MahasiswaApi
import com.jaya.gd11_a_0804.models.Mahasiswa
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {
    private var srMahasiswa: SwipeRefreshLayout? = null
    private var adapter: MahasiswaAdapter? = null
    private var svMahasiswa: SearchView? = null
    private var layoutLoading: LinearLayout? = null
    private var queue: RequestQueue? = null

    companion object{
        const val LAUNCH_ADD_ACTIVITY = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        queue = Volley.newRequestQueue(this)
        layoutLoading = findViewById(R.id.layout_loading)
        srMahasiswa = findViewById(R.id.sr_mahasiswa)
        svMahasiswa = findViewById(R.id.sv_mahasiswa)

        srMahasiswa?.setOnRefreshListener ({ allMahasiswa() })
        svMahasiswa?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(s: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(s: String?): Boolean {
                adapter!!.filter.filter(s)
                return false
            }
        })

        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)
        fabAdd.setOnClickListener{
            val i = Intent(this@MainActivity, AddEditActivity::class.java )
            startActivityForResult(i, LAUNCH_ADD_ACTIVITY)
        }

        val rvProduk = findViewById<RecyclerView>(R.id.rv_mahasiswa)
        adapter = MahasiswaAdapter(ArrayList(), this)
        rvProduk.layoutManager = LinearLayoutManager(this)
        rvProduk.adapter = adapter
        allMahasiswa()
    }

    private fun allMahasiswa(){
        srMahasiswa!!.isRefreshing = true
        val stringRequest: StringRequest = object :
            StringRequest(Method.GET, MahasiswaApi.GET_ALL_URL, Response.Listener { response ->
                val gson = Gson()
                val mahasiswa : Array<Mahasiswa> = gson.fromJson(response, Array<Mahasiswa>::class.java)

                adapter!!.setMahasiswaList(mahasiswa)
                adapter!!.filter.filter(svMahasiswa!!.query)
                srMahasiswa!!.isRefreshing = false
                Log.d("maaa", mahasiswa.get(0).nama.toString())
                if(!mahasiswa.isEmpty())
                    Toast.makeText(this@MainActivity, "Data berhasil diambil", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@MainActivity, "Data kosong", Toast.LENGTH_SHORT).show()

            }, Response.ErrorListener { error ->
                srMahasiswa!!.isRefreshing = false
                try{
                    val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                    val errors = JSONObject(responseBody)
                    Toast.makeText(
                        this@MainActivity,
                        errors.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                catch (e: Exception){
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>{
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                return headers
            }}
        queue!!.add(stringRequest)
    }

    public fun deleteMahasiswa(id: Long){
        setLoading(true)
        val stringRequest: StringRequest = object :
            StringRequest(Method.DELETE, MahasiswaApi.DELETE_URL + id, Response.Listener { response ->
                setLoading(false)
                val gson = Gson()
                val mahasiswa  = gson.fromJson(response, Mahasiswa::class.java)

                if(mahasiswa != null)
                    Toast.makeText(this@MainActivity, "Data berhasil diambil", Toast.LENGTH_SHORT).show()

                allMahasiswa()
            }, Response.ErrorListener { error ->
                setLoading(false)
                try{
                    val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                    val errors = JSONObject(responseBody)
                    Toast.makeText(
                        this@MainActivity,
                        errors.getString("message"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                catch (e: Exception){
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }){
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String>{
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                return headers
            }}
        queue!!.add(stringRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == LAUNCH_ADD_ACTIVITY && resultCode == RESULT_OK) allMahasiswa()
    }

    private fun setLoading(isLoading: Boolean){
        if(isLoading){
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            layoutLoading!!.visibility = View.VISIBLE
        }
        else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            layoutLoading!!.visibility = View.INVISIBLE
        }
    }
}