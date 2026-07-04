package io.github.hotroso.vietnameselunarcalendar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.hotroso.vietnameselunarcalendar.lunar.CanChi
import io.github.hotroso.vietnameselunarcalendar.lunar.EventManager
import io.github.hotroso.vietnameselunarcalendar.lunar.VietCalendar

/**
 * BottomSheet dialog hiển thị chi tiết ngày qua WebView + date_detail.html template.
 * Trung thành 100% với flow gốc: render HTML với placeholders.
 */
class DateDetailFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_MONTH = "month"
        private const val ARG_YEAR = "year"

        fun newInstance(day: Int, month: Int, year: Int): DateDetailFragment {
            return DateDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_DAY, day)
                    putInt(ARG_MONTH, month)
                    putInt(ARG_YEAR, year)
                }
            }
        }
    }

    private var solarDay = 1
    private var solarMonth = 1
    private var solarYear = 2024

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            solarDay = it.getInt(ARG_DAY)
            solarMonth = it.getInt(ARG_MONTH)
            solarYear = it.getInt(ARG_YEAR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_date_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeBtn = view.findViewById<Button>(R.id.closeButton)
        val shareBtn = view.findViewById<Button>(R.id.shareButton)
        val webView = view.findViewById<WebView>(R.id.myWebView)

        // Compute all data
        val lunar = VietCalendar.solarToLunar(solarDay, solarMonth, solarYear)
        val info = CanChi.getFullInfo(lunar)
        val jdn = lunar.julianDay
        val daysOfWeek = arrayOf(
            "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm",
            "Thứ sáu", "Thứ bảy", "Chủ nhật"
        )
        val dowIndex = VietCalendar.getDayOfWeek(solarDay, solarMonth, solarYear)
        val dayOfWeek = daysOfWeek[dowIndex]

        // Build HTML
        val html = buildDetailHtml(lunar, info, jdn, dayOfWeek)

        // Load into WebView
        webView.settings.defaultTextEncodingName = "utf-8"
        webView.loadDataWithBaseURL(
            "file:///android_asset/",
            html,
            "text/html",
            "utf-8",
            null
        )

        // Close button
        closeBtn.setOnClickListener { dismiss() }

        // Share button
        shareBtn.setOnClickListener {
            shareDetails(lunar, info, dayOfWeek)
        }
    }

    private fun buildDetailHtml(
        lunar: io.github.hotroso.vietnameselunarcalendar.lunar.LunarDate,
        info: CanChi.CanChiInfo,
        jdn: Int,
        dayOfWeek: String
    ): String {
        // Load template
        var template = try {
            requireContext().assets.open("date_detail.html")
                .bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            return "<html><body>Lỗi tải template</body></html>"
        }

        // Ngày dương lịch
        val ngayDuongLich = "$dayOfWeek, $solarDay/$solarMonth/$solarYear"

        // Hoàng Đạo / Hắc Đạo
        val (isHoangDao, hoangDaoInfo) = CanChi.getHoangDaoHacDao(jdn)
        val ngayHoangDaoHtml = if (isHoangDao) {
            "<p id=\"tvNgayHoangdao\" data-hd=\"1\">★ $hoangDaoInfo</p>"
        } else {
            "<p id=\"tvNgayHoangdao\">$hoangDaoInfo</p>"
        }

        // Ngày âm lịch
        val leapStr = if (lunar.isLeapMonth == 1) " (nhuận)" else ""
        val ngayAmLich = "<b>Âm lịch:</b> Ngày ${lunar.day} tháng ${CanChi.tenThangAm(lunar.month)}$leapStr năm ${CanChi.canChiNam(lunar.year)}"

        // Bát tự
        val ngayAmLichBatTu = "<b>Can Chi:</b> Ngày ${info.ngay} - Tháng ${info.thang} - Năm ${info.nam} - Giờ ${info.gio}"

        // Giờ hoàng đạo
        val gioHoangDao = "<b>Giờ hoàng đạo:</b> ${info.gioHoangDao}"

        // Mệnh ngày (Ngũ hành nạp âm)
        val menhNgay = "<b>Ngũ hành nạp âm:</b> ${CanChi.getNguHanhNapAm(jdn)}"

        // Tiết khí
        val tietKhi = "<b>Tiết khí:</b> ${info.tietKhi}"

        // Trực
        val truc = "<b>Trực:</b> ${CanChi.getTruc(jdn)}"
        val trucDetail = "<i>${CanChi.getTrucDetail(jdn)}</i>"

        // Tuổi xung khắc
        val tuoiXungKhac = CanChi.getTuoiXungKhac(jdn)

        // Hướng xuất hành
        val taiThan = CanChi.getTaiThan(jdn)
        val hyThan = CanChi.getHyThan(jdn)
        val hacThan = CanChi.getHacThan(jdn)

        // Nhị thập bát tú
        val nhatBatTuIndex = CanChi.getNhiThapBatTu(jdn)
        val nhiThapBatTu = loadNhiThapBatTu(nhatBatTuIndex)

        // Replace placeholders
        template = template.replace("[tvNgayduonglich]", ngayDuongLich)
        template = template.replace("[tvNgayHoangdao]", ngayHoangDaoHtml)
        template = template.replace("[tvNgayAmlich]", ngayAmLich)
        template = template.replace("[tvNgayAmlichBattu]", ngayAmLichBatTu)
        template = template.replace("[tvGioHoangDao]", gioHoangDao)
        template = template.replace("[tvMenhngay]", menhNgay)
        template = template.replace("[tvTietkhi]", tietKhi)
        template = template.replace("[tvTruc]", truc)
        template = template.replace("[tvTrucDetail]", trucDetail)
        template = template.replace("[tvTuoixungkhac]", tuoiXungKhac)
        template = template.replace("[tvTaithan]", taiThan)
        template = template.replace("[tvHythan]", hyThan)
        template = template.replace("[tvHacthan]", hacThan)
        template = template.replace("[tvNhithapBattu]", nhiThapBatTu)

        return template
    }

    private fun loadNhiThapBatTu(index: Int): String {
        return try {
            requireContext().assets.open("28sao/$index.html")
                .bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Không có thông tin"
        }
    }

    private fun shareDetails(
        lunar: io.github.hotroso.vietnameselunarcalendar.lunar.LunarDate,
        info: CanChi.CanChiInfo,
        dayOfWeek: String
    ) {
        val leapStr = if (lunar.isLeapMonth == 1) " (nhuận)" else ""
        val jdn = lunar.julianDay
        val sb = StringBuilder()
        sb.appendLine("📅 $dayOfWeek, ngày $solarDay/$solarMonth/$solarYear")
        sb.appendLine("🌙 Âm lịch: Ngày ${lunar.day} tháng ${CanChi.tenThangAm(lunar.month)}$leapStr năm ${CanChi.canChiNam(lunar.year)}")
        sb.appendLine("📌 Ngày ${info.ngay}, Tháng ${info.thang}, Năm ${info.nam}")
        sb.appendLine("🌿 Tiết khí: ${info.tietKhi}")
        sb.appendLine("⏰ Giờ hoàng đạo: ${info.gioHoangDao}")
        sb.appendLine("🏷 Trực: ${CanChi.getTruc(jdn)}")
        sb.appendLine("🧭 Tài thần: ${CanChi.getTaiThan(jdn)} | Hỷ thần: ${CanChi.getHyThan(jdn)}")

        val eventManager = EventManager(requireContext())
        val events = eventManager.getEventsForDate(solarDay, solarMonth, lunar.day, lunar.month)
        if (events.isNotEmpty()) {
            sb.appendLine("🎉 ${events.joinToString(", ")}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Âm lịch - Ngày $solarDay/$solarMonth/$solarYear")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_with)))
    }
}
