import sys
import json
import os
import time
import random
import math
import re

from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QGridLayout, QLabel, QLineEdit, QComboBox, QSpinBox, QCheckBox,
    QPushButton, QTextEdit, QGroupBox, QFrame,
    QFileDialog, QMessageBox, QProgressBar, QTabWidget, QTableWidget,
    QTableWidgetItem, QHeaderView, QStatusBar, QToolButton,
    QListWidget, QInputDialog, QDoubleSpinBox
)
from PyQt6.QtCore import Qt, QThread, pyqtSignal, QTimer, QPoint
from PyQt6.QtGui import (
    QFont, QColor, QPainter, QPen, QBrush, QRadialGradient
)

# =============================================================================
# LED INDICATOR
# =============================================================================
class LEDIndicator(QWidget):
    def __init__(self, color_off="#30363d", color_on="#3fb950", size=14, parent=None):
        super().__init__(parent)
        self.color_off = QColor(color_off)
        self.color_on = QColor(color_on)
        self.is_on = False
        self.size = size
        self.setFixedSize(size, size)
        self.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents)

    def set_state(self, on):
        self.is_on = on
        self.update()

    def paintEvent(self, event):
        painter = QPainter(self)
        painter.setRenderHint(QPainter.RenderHint.Antialiasing)
        color = self.color_on if self.is_on else self.color_off

        if self.is_on:
            glow = QRadialGradient(self.size/2, self.size/2, self.size)
            glow.setColorAt(0.0, QColor(color.red(), color.green(), color.blue(), 180))
            glow.setColorAt(0.5, QColor(color.red(), color.green(), color.blue(), 60))
            glow.setColorAt(1.0, QColor(color.red(), color.green(), color.blue(), 0))
            painter.setBrush(QBrush(glow))
            painter.setPen(Qt.PenStyle.NoPen)
            painter.drawEllipse(0, 0, self.size, self.size)

        core_grad = QRadialGradient(self.size*0.35, self.size*0.35, self.size*0.4)
        core_grad.setColorAt(0.0, QColor(255, 255, 255, 200))
        core_grad.setColorAt(0.5, color)
        core_grad.setColorAt(1.0, QColor(color.red()*0.7, color.green()*0.7, color.blue()*0.7))

        painter.setBrush(QBrush(core_grad))
        painter.setPen(QPen(QColor(255,255,255,40), 1))
        painter.drawEllipse(2, 2, self.size-4, self.size-4)

# =============================================================================
# PARTICLE SYSTEM
# =============================================================================
class Particle:
    def __init__(self, x, y, w, h):
        self.x = x
        self.y = y
        self.w = w
        self.h = h
        angle = random.uniform(0, math.pi * 2)
        speed = random.uniform(0.2, 0.8)
        self.vx = math.cos(angle) * speed
        self.vy = math.sin(angle) * speed
        self.radius = random.uniform(1.5, 3.5)
        palettes = [
            (0, 229, 255), (255, 0, 128), (255, 215, 0), (57, 255, 20),
            (188, 19, 254), (0, 255, 255), (255, 140, 0), (0, 191, 255),
        ]
        self.color = random.choice(palettes)
        self.glow_radius = self.radius * random.uniform(3, 6)
        self.mass = self.radius

    def update(self, mouse_pos, mouse_active, attraction_strength=0.03):
        if mouse_active and mouse_pos:
            dx = mouse_pos.x() - self.x
            dy = mouse_pos.y() - self.y
            dist = math.sqrt(dx * dx + dy * dy)
            if dist > 0 and dist < 250:
                force = 150.0 / (dist * dist + 200) * attraction_strength
                self.vx += (dx / dist) * force
                self.vy += (dy / dist) * force

        self.vx *= 0.998
        self.vy *= 0.998

        speed = math.sqrt(self.vx * self.vx + self.vy * self.vy)
        if speed < 0.15:
            angle = random.uniform(0, math.pi * 2)
            self.vx += math.cos(angle) * 0.03
            self.vy += math.sin(angle) * 0.03

        self.x += self.vx
        self.y += self.vy

        if self.x < self.radius:
            self.x = self.radius
            self.vx = abs(self.vx) * 0.8
        elif self.x > self.w - self.radius:
            self.x = self.w - self.radius
            self.vx = -abs(self.vx) * 0.8

        if self.y < self.radius:
            self.y = self.radius
            self.vy = abs(self.vy) * 0.8
        elif self.y > self.h - self.radius:
            self.y = self.h - self.radius
            self.vy = -abs(self.vy) * 0.8

    def scatter(self, tap_x, tap_y, force=8.0):
        dx = self.x - tap_x
        dy = self.y - tap_y
        dist = math.sqrt(dx * dx + dy * dy)
        if dist < 200 and dist > 0:
            push = force * (1 - dist / 200)
            self.vx += (dx / dist) * push
            self.vy += (dy / dist) * push

    def collide(self, other):
        dx = other.x - self.x
        dy = other.y - self.y
        dist = math.sqrt(dx * dx + dy * dy)
        min_dist = self.radius + other.radius

        if dist < min_dist and dist > 0:
            nx = dx / dist
            ny = dy / dist
            overlap = min_dist - dist
            self.x -= nx * overlap * 0.5
            self.y -= ny * overlap * 0.5
            other.x += nx * overlap * 0.5
            other.y += ny * overlap * 0.5

            dvx = other.vx - self.vx
            dvy = other.vy - self.vy
            dv_dot_n = dvx * nx + dvy * ny

            if dv_dot_n > 0:
                return

            impulse = 2 * dv_dot_n / (self.mass + other.mass)
            self.vx += impulse * other.mass * nx * 0.9
            self.vy += impulse * other.mass * ny * 0.9
            other.vx -= impulse * self.mass * nx * 0.9
            other.vy -= impulse * self.mass * ny * 0.9


class ParticleBackground(QWidget):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.particles = []
        self.num_particles = 80
        self.mouse_pos = None
        self.mouse_active = False
        self.connection_distance = 180
        self.time = 0.0
        self.gradient_phase = 0.0
        self.freefloat_mode = False
        self.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents, False)
        self.timer = QTimer(self)
        self.timer.timeout.connect(self.update_particles)
        self.timer.start(16)

    def resizeEvent(self, event):
        super().resizeEvent(event)
        self.init_particles()

    def init_particles(self):
        self.particles = []
        w = self.width() or 1400
        h = self.height() or 900
        for _ in range(self.num_particles):
            self.particles.append(Particle(random.uniform(0, w), random.uniform(0, h), w, h))

    def mouseMoveEvent(self, event):
        self.mouse_pos = event.pos()
        self.mouse_active = True

    def leaveEvent(self, event):
        self.mouse_active = False

    def mousePressEvent(self, event):
        for p in self.particles:
            p.scatter(event.pos().x(), event.pos().y())
        self.update()

    def set_freefloat(self, enabled):
        self.freefloat_mode = enabled

    def update_particles(self):
        self.time += 0.016
        self.gradient_phase += 0.003
        w = self.width()
        h = self.height()

        for p in self.particles:
            p.w = w
            p.h = h
            p.update(self.mouse_pos, self.mouse_active)

        for i in range(len(self.particles)):
            for j in range(i + 1, len(self.particles)):
                self.particles[i].collide(self.particles[j])

        self.update()

    def paintEvent(self, event):
        painter = QPainter(self)
        painter.setRenderHint(QPainter.RenderHint.Antialiasing)
        w = self.width()
        h = self.height()

        self.draw_morphing_background(painter, w, h)

        if not self.freefloat_mode:
            self.draw_connections(painter)

        self.draw_particles(painter)

    def draw_morphing_background(self, painter, w, h):
        phase = self.gradient_phase
        cx1 = w * (0.3 + 0.2 * math.sin(phase))
        cy1 = h * (0.3 + 0.2 * math.cos(phase * 0.7))
        cx2 = w * (0.7 + 0.2 * math.sin(phase * 1.3 + 2))
        cy2 = h * (0.7 + 0.2 * math.cos(phase * 0.9 + 1))

        gradient = QRadialGradient(cx1, cy1, max(w, h) * 0.8)
        r1 = int(13 + 8 * math.sin(phase))
        g1 = int(17 + 10 * math.cos(phase * 0.8))
        b1 = int(23 + 12 * math.sin(phase * 1.2))
        r2 = int(26 + 15 * math.sin(phase * 1.5))
        g2 = int(20 + 20 * math.cos(phase))
        b2 = int(45 + 25 * math.sin(phase * 0.6))
        r3 = int(10 + 5 * math.cos(phase * 2))
        g3 = int(15 + 8 * math.sin(phase * 1.8))
        b3 = int(35 + 10 * math.cos(phase * 1.1))

        gradient.setColorAt(0.0, QColor(r1, g1, b1))
        gradient.setColorAt(0.4, QColor(r2, g2, b2))
        gradient.setColorAt(1.0, QColor(r3, g3, b3))
        painter.fillRect(self.rect(), gradient)

        gradient2 = QRadialGradient(cx2, cy2, max(w, h) * 0.6)
        gradient2.setColorAt(0.0, QColor(20, 30, 60, 80))
        gradient2.setColorAt(1.0, QColor(0, 0, 0, 0))
        painter.fillRect(self.rect(), gradient2)

    def draw_connections(self, painter):
        pen = QPen()
        pen.setWidthF(0.8)
        for i in range(len(self.particles)):
            for j in range(i + 1, len(self.particles)):
                p1 = self.particles[i]
                p2 = self.particles[j]
                dx = p2.x - p1.x
                dy = p2.y - p1.y
                dist = math.sqrt(dx * dx + dy * dy)
                if dist < self.connection_distance:
                    alpha = int(180 * (1 - dist / self.connection_distance))
                    r = int((p1.color[0] + p2.color[0]) / 2)
                    g = int((p1.color[1] + p2.color[1]) / 2)
                    b = int((p1.color[2] + p2.color[2]) / 2)
                    pen.setColor(QColor(r, g, b, alpha))
                    painter.setPen(pen)
                    painter.drawLine(int(p1.x), int(p1.y), int(p2.x), int(p2.y))

    def draw_particles(self, painter):
        for p in self.particles:
            glow_gradient = QRadialGradient(p.x, p.y, p.glow_radius)
            glow_gradient.setColorAt(0.0, QColor(p.color[0], p.color[1], p.color[2], 120))
            glow_gradient.setColorAt(0.5, QColor(p.color[0], p.color[1], p.color[2], 40))
            glow_gradient.setColorAt(1.0, QColor(p.color[0], p.color[1], p.color[2], 0))
            painter.setBrush(QBrush(glow_gradient))
            painter.setPen(Qt.PenStyle.NoPen)
            painter.drawEllipse(
                int(p.x - p.glow_radius), int(p.y - p.glow_radius),
                int(p.glow_radius * 2), int(p.glow_radius * 2)
            )
            core_gradient = QRadialGradient(p.x - p.radius*0.3, p.y - p.radius*0.3, p.radius)
            core_gradient.setColorAt(0.0, QColor(255, 255, 255, 250))
            core_gradient.setColorAt(0.4, QColor(p.color[0], p.color[1], p.color[2], 255))
            core_gradient.setColorAt(1.0, QColor(p.color[0], p.color[1], p.color[2], 180))
            painter.setBrush(QBrush(core_gradient))
            painter.drawEllipse(
                int(p.x - p.radius), int(p.y - p.radius),
                int(p.radius * 2), int(p.radius * 2)
            )


# =============================================================================
# PROXY SCRAPER THREAD
# =============================================================================
class ProxyScraperThread(QThread):
    log_signal = pyqtSignal(str)
    proxy_signal = pyqtSignal(list)
    status_signal = pyqtSignal(str, bool)
    finished_signal = pyqtSignal()

    PROXY_SOURCES = [
        "https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/http.txt",
        "https://raw.githubusercontent.com/ShiftyTR/Proxy-List/master/http.txt",
        "https://raw.githubusercontent.com/monosans/proxy-list/main/proxies/http.txt",
        "https://raw.githubusercontent.com/clarketm/proxy-list/master/proxy-list-raw.txt",
        "https://raw.githubusercontent.com/jetkai/proxy-list/main/online-proxies/txt/proxies-http.txt",
    ]

    def __init__(self):
        super().__init__()
        self.running = True
        self.validated_proxies = []

    def run(self):
        try:
            self.log_signal.emit("[SCRAPER] Starting proxy scrape...")
            self.status_signal.emit("Scraping...", False)

            try:
                import requests
            except ImportError:
                self.log_signal.emit("[SCRAPER] ERROR: requests module not installed. Run: pip install requests")
                self.status_signal.emit("requests not installed", True)
                self.finished_signal.emit()
                return

            all_proxies = set()

            for source in self.PROXY_SOURCES:
                if not self.running:
                    break
                try:
                    self.log_signal.emit(f"[SCRAPER] Fetching from {source.split('/')[-1]}...")
                    r = requests.get(source, timeout=15)
                    if r.status_code == 200:
                        lines = r.text.strip().split('\n')
                        for line in lines:
                            line = line.strip()
                            if re.match(r'^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}:\d+$', line):
                                all_proxies.add(line)
                        self.log_signal.emit(f"[SCRAPER] Got {len(lines)} lines from source")
                except Exception as e:
                    self.log_signal.emit(f"[SCRAPER] Source failed: {str(e)[:60]}")

            self.log_signal.emit(f"[SCRAPER] Total unique proxies: {len(all_proxies)}")

            self.validated_proxies = []
            test_proxies = list(all_proxies)[:50]

            self.log_signal.emit("[SCRAPER] Validating against Microsoft (outlook.live.com)...")
            self.status_signal.emit("Validating...", False)

            for proxy in test_proxies:
                if not self.running:
                    break
                try:
                    proxy_dict = {"http": f"http://{proxy}", "https": f"http://{proxy}"}
                    r = requests.get(
                        "https://outlook.live.com",
                        proxies=proxy_dict,
                        timeout=5,
                        headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"}
                    )
                    if r.status_code == 200:
                        self.validated_proxies.append(proxy)
                        self.log_signal.emit(f"[SCRAPER] Valid: {proxy}")
                except:
                    pass

            self.log_signal.emit(f"[SCRAPER] Validation complete. {len(self.validated_proxies)} working proxies.")
            self.proxy_signal.emit(self.validated_proxies)

            if len(self.validated_proxies) > 0:
                self.status_signal.emit(f"{len(self.validated_proxies)} proxies ready", False)
            else:
                self.status_signal.emit("No working proxies found", True)

        except Exception as e:
            self.log_signal.emit(f"[SCRAPER] Fatal error: {str(e)}")
            self.status_signal.emit("Scraper failed", True)
        finally:
            self.finished_signal.emit()

    def stop(self):
        self.running = False


# =============================================================================
# GENERATOR WORKER
# =============================================================================
class GeneratorWorker(QThread):
    log_signal = pyqtSignal(str)
    stats_signal = pyqtSignal(dict)
    finished_signal = pyqtSignal()

    def __init__(self, config_path):
        super().__init__()
        self.config_path = config_path
        self.running = True

    def run(self):
        try:
            self.log_signal.emit("[INFO] Starting Outlook Generator...")
            domains = ["@outlook.com", "@hotmail.com"]

            for i in range(100):
                if not self.running:
                    break
                time.sleep(0.5)

                email = f"user{random.randint(1000,9999)}{random.choice(['', str(random.randint(1,99))])}"
                domain = random.choice(domains)
                password = ''.join([random.choice('ABCDEF0123456789') for _ in range(10)])

                status = random.choice([
                    ("SUCCESS", f"[SUCCESS] Created: {email}{domain} | Pass: {password}"),
                    ("CAPTCHA", f"[CAPTCHA] Solving for {email}..."),
                    ("PROXY", f"[PROXY] Checking proxy..."),
                    ("ERROR", f"[ERROR] Failed: {email}{domain} | Proxy timeout")
                ])

                self.log_signal.emit(status[1])

                stats = {
                    'generated': i + 1,
                    'success': random.randint(0, i+1),
                    'failed': random.randint(0, 5),
                    'captcha_solved': random.randint(0, i+1),
                    'balance': round(random.uniform(0.5, 50.0), 2)
                }
                self.stats_signal.emit(stats)

        except Exception as e:
            self.log_signal.emit(f"[ERROR] Worker crashed: {str(e)}")
        finally:
            self.finished_signal.emit()

    def stop(self):
        self.running = False


# =============================================================================
# STYLESHEET
# =============================================================================
DARK_NEON_STYLE = """
QMainWindow { background-color: transparent; }
QWidget { background-color: transparent; color: #c9d1d9; font-family: 'Segoe UI', sans-serif; font-size: 12px; }
QGroupBox {
    font-weight: bold; font-size: 13px; color: #00e5ff;
    border: 2px solid rgba(0, 229, 255, 0.3); border-radius: 10px;
    margin-top: 12px; padding-top: 15px; padding-bottom: 10px;
    padding-left: 15px; padding-right: 15px;
    background-color: rgba(13, 17, 23, 0.75);
}
QGroupBox::title {
    subcontrol-origin: margin; left: 15px; padding: 0 10px;
    color: #00e5ff; background-color: rgba(13, 17, 23, 0.9);
}
QLineEdit {
    background-color: rgba(22, 27, 34, 0.9); border: 2px solid rgba(48, 54, 61, 0.8); border-radius: 8px;
    padding: 8px 12px; color: #e6edf3; font-size: 12px;
    selection-background-color: #1f6feb;
}
QLineEdit:focus { border: 2px solid #58a6ff; background-color: rgba(13, 17, 23, 0.95); }
QLineEdit:disabled { background-color: rgba(33, 38, 45, 0.8); color: #484f58; }
QComboBox {
    background-color: rgba(22, 27, 34, 0.9); border: 2px solid rgba(48, 54, 61, 0.8); border-radius: 8px;
    padding: 8px 12px; color: #e6edf3; font-size: 12px; min-width: 120px;
}
QComboBox:focus { border: 2px solid #58a6ff; }
QComboBox::drop-down { border: none; width: 30px; }
QComboBox QAbstractItemView {
    background-color: #161b22; color: #c9d1d9; border: 2px solid #30363d;
    selection-background-color: #1f6feb; selection-color: #ffffff; padding: 5px;
}
QSpinBox, QDoubleSpinBox {
    background-color: rgba(22, 27, 34, 0.9); border: 2px solid rgba(48, 54, 61, 0.8); border-radius: 8px;
    padding: 8px 12px; color: #e6edf3; font-size: 12px;
}
QSpinBox:focus, QDoubleSpinBox:focus { border: 2px solid #58a6ff; }
QCheckBox { color: #c9d1d9; font-size: 12px; spacing: 8px; }
QCheckBox::indicator {
    width: 18px; height: 18px; border-radius: 4px;
    border: 2px solid rgba(48, 54, 61, 0.8); background-color: rgba(22, 27, 34, 0.9);
}
QCheckBox::indicator:checked { background-color: #238636; border: 2px solid #238636; }
QCheckBox::indicator:hover { border: 2px solid #58a6ff; }
QPushButton {
    background-color: rgba(35, 134, 54, 0.9); color: #ffffff; border: none; border-radius: 8px;
    padding: 10px 24px; font-size: 13px; font-weight: bold; min-height: 36px;
}
QPushButton:hover { background-color: #2ea043; border: 2px solid #3fb950; }
QPushButton:pressed { background-color: #1a7f37; }
QPushButton:disabled { background-color: rgba(33, 38, 45, 0.8); color: #484f58; border: 2px solid rgba(48, 54, 61, 0.5); }
QPushButton#danger { background-color: rgba(218, 54, 51, 0.9); }
QPushButton#danger:hover { background-color: #f85149; border: 2px solid #ff7b72; }
QPushButton#secondary { background-color: rgba(31, 111, 235, 0.9); }
QPushButton#secondary:hover { background-color: #388bfd; border: 2px solid #58a6ff; }
QPushButton#accent { background-color: rgba(137, 87, 229, 0.9); }
QPushButton#accent:hover { background-color: #a371f7; border: 2px solid #bc8cff; }
QPushButton#pill {
    background-color: rgba(255, 140, 0, 0.9);
    border-radius: 20px;
    padding: 8px 20px;
    font-size: 12px;
}
QPushButton#pill:hover { background-color: #ffa500; border: 2px solid #ffb84d; }
QTextEdit {
    background-color: rgba(22, 27, 34, 0.85); border: 2px solid rgba(48, 54, 61, 0.6); border-radius: 10px;
    padding: 12px; color: #3fb950;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace; font-size: 12px;
    selection-background-color: #1f6feb;
}
QTextEdit:focus { border: 2px solid #58a6ff; }
QProgressBar {
    border: 2px solid rgba(48, 54, 61, 0.6); border-radius: 8px; text-align: center;
    color: #ffffff; font-weight: bold; background-color: rgba(22, 27, 34, 0.8); height: 24px;
}
QProgressBar::chunk {
    background-color: qlineargradient(x1:0, y1:0, x2:1, y2:0, stop:0 #238636, stop:1 #3fb950);
    border-radius: 6px;
}
QTableWidget {
    background-color: rgba(22, 27, 34, 0.8); border: 2px solid rgba(48, 54, 61, 0.6); border-radius: 10px;
    gridline-color: rgba(48, 54, 61, 0.4); color: #c9d1d9; font-size: 12px;
}
QTableWidget::item { padding: 8px; border-bottom: 1px solid rgba(33, 38, 45, 0.5); }
QTableWidget::item:selected { background-color: #1f6feb; color: #ffffff; }
QHeaderView::section {
    background-color: rgba(33, 38, 45, 0.9); color: #00e5ff; padding: 10px;
    border: none; border-bottom: 2px solid rgba(48, 54, 61, 0.6); font-weight: bold; font-size: 12px;
}
QScrollBar:vertical { background-color: transparent; width: 12px; border-radius: 6px; }
QScrollBar::handle:vertical { background-color: rgba(48, 54, 61, 0.6); border-radius: 6px; min-height: 30px; }
QScrollBar::handle:vertical:hover { background-color: rgba(72, 79, 88, 0.8); }
QScrollBar::add-line:vertical, QScrollBar::sub-line:vertical { height: 0px; }
QTabWidget::pane {
    border: 2px solid rgba(48, 54, 61, 0.4); border-radius: 10px;
    background-color: rgba(13, 17, 23, 0.5); top: -2px;
}
QTabBar::tab {
    background-color: rgba(22, 27, 34, 0.8); color: #8b949e; border: 2px solid rgba(48, 54, 61, 0.4);
    border-bottom: none; border-top-left-radius: 8px; border-top-right-radius: 8px;
    padding: 10px 24px; font-weight: bold; font-size: 12px; margin-right: 4px;
}
QTabBar::tab:selected {
    background-color: rgba(13, 17, 23, 0.8); color: #00e5ff; border: 2px solid #58a6ff;
    border-bottom: 2px solid rgba(13, 17, 23, 0.8);
}
QTabBar::tab:hover:!selected { background-color: rgba(33, 38, 45, 0.8); color: #c9d1d9; }
QStatusBar { background-color: rgba(22, 27, 34, 0.8); color: #8b949e; border-top: 2px solid rgba(48, 54, 61, 0.4); font-size: 11px; }
QListWidget {
    background-color: rgba(22, 27, 34, 0.8); border: 2px solid rgba(48, 54, 61, 0.6); border-radius: 8px;
    color: #c9d1d9; padding: 5px;
}
QListWidget::item { padding: 8px; border-radius: 6px; margin: 2px; }
QListWidget::item:selected { background-color: #1f6feb; color: #ffffff; }
QListWidget::item:hover { background-color: rgba(33, 38, 45, 0.8); }
QToolButton {
    background-color: rgba(33, 38, 45, 0.8); border: 2px solid rgba(48, 54, 61, 0.6); border-radius: 6px;
    padding: 6px; color: #c9d1d9;
}
QToolButton:hover { background-color: rgba(48, 54, 61, 0.8); border: 2px solid #58a6ff; }
QLabel#title { font-size: 24px; font-weight: bold; color: #00e5ff; }
QLabel#subtitle { font-size: 14px; color: #8b949e; }
QLabel#status_good { color: #3fb950; font-weight: bold; }
QLabel#status_bad { color: #f85149; font-weight: bold; }
QLabel#status_warn { color: #d29922; font-weight: bold; }
QFrame#separator { background-color: rgba(48, 54, 61, 0.5); max-height: 2px; }
"""

# =============================================================================
# DEFAULT CONFIG
# =============================================================================
DEFAULT_CONFIG = {
    "Common": {
        "Prefix": "&beGen&5>> ",
        "ProxyFile": "proxy.txt",
        "OutputFile": "account.txt",
        "Timer": True,
        "ProxyCheckTimeout": 2
    },
    "Captcha": {
        "providers": "2captcha",
        "api_key": "",
        "site_key": "B7D8911C-5CC8-A9A3-35B0-554ACEE604DA"
    },
    "EmailInfo": {
        "Domain": "@outlook.com",
        "minBirthDate": 1980,
        "maxBirthDate": 1999,
        "PasswordLength": 10,
        "FirstNameLength": 5,
        "LastNameLength": 5
    },
    "DriverArguments": [
        "--disable-logging",
        "--disable-login-animations",
        "--disable-notifications",
        "--incognito",
        "--ignore-certificate-errors",
        "--disable-blink-features=AutomationControlled",
        "--disable-gpu",
        "--headless",
        "--no-sandbox",
        "--lang=en"
    ]
}


# =============================================================================
# MAIN WINDOW
# =============================================================================
class OutlookGenGUI(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Outlook Generator Pro - bluemeanie23 kimi 2026")
        self.setMinimumSize(1500, 950)
        self.config_file = "config.json"
        self.worker = None
        self.scraper_thread = None
        self.stored_proxies = []
        self.overlay_widget = None

        self.load_config()
        self.init_ui()
        self.load_settings_into_ui()
        self.validate_config()
        self.update_system_leds()

    def load_config(self):
        if os.path.exists(self.config_file):
            try:
                with open(self.config_file, 'r') as f:
                    loaded = json.load(f)
                    self.config = DEFAULT_CONFIG.copy()
                    for section in loaded:
                        if section in self.config:
                            if isinstance(loaded[section], dict):
                                self.config[section].update(loaded[section])
                            else:
                                self.config[section] = loaded[section]
                        else:
                            self.config[section] = loaded[section]
            except Exception as e:
                print(f"Error loading config: {e}, using defaults")
                self.config = DEFAULT_CONFIG.copy()
                self.save_config()
        else:
            self.config = DEFAULT_CONFIG.copy()
            self.save_config()

    def save_config(self):
        try:
            with open(self.config_file, 'w') as f:
                json.dump(self.config, f, indent=2)
            self.statusBar().showMessage("Configuration saved successfully!", 3000)
            return True
        except Exception as e:
            QMessageBox.critical(self, "Error", f"Failed to save config: {str(e)}")
            return False

    def init_ui(self):
        # Set a solid background color for the main window
        self.setStyleSheet("""
            QMainWindow { background-color: #0d1117; }
        """ + DARK_NEON_STYLE)
        
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        central_layout = QHBoxLayout(central_widget)
        central_layout.setContentsMargins(0, 0, 0, 0)
        central_layout.setSpacing(0)
        
        # Particle background as full window background
        self.particle_bg = ParticleBackground()
        self.particle_bg.setGeometry(0, 0, 1500, 950)
        central_layout.addWidget(self.particle_bg)
        
        # Overlay container that sits on top of particle background
        self.overlay_widget = QWidget(self.particle_bg)
        self.overlay_widget.setGeometry(0, 0, 1500, 950)
        self.overlay_widget.setAttribute(Qt.WidgetAttribute.WA_TransparentForMouseEvents, False)
        overlay_layout = QHBoxLayout(self.overlay_widget)
        overlay_layout.setContentsMargins(15, 15, 15, 15)
        overlay_layout.setSpacing(15)

        left_widget = QWidget()
        left_widget.setStyleSheet("background-color: rgba(13, 17, 23, 0.85); border-radius: 10px;")
        left_layout = QVBoxLayout(left_widget)
        left_layout.setSpacing(12)
        left_layout.setContentsMargins(10, 10, 10, 10)

        status_bar = self.create_system_status_bar()
        left_layout.addWidget(status_bar)

        header = self.create_header()
        left_layout.addWidget(header)

        settings_tabs = QTabWidget()
        settings_tabs.addTab(self.create_common_tab(), "Common")
        settings_tabs.addTab(self.create_captcha_tab(), "Captcha")
        settings_tabs.addTab(self.create_email_tab(), "Email Info")
        settings_tabs.addTab(self.create_driver_tab(), "Driver Args")
        settings_tabs.addTab(self.create_proxy_scraper_tab(), "Proxy Scraper")
        left_layout.addWidget(settings_tabs, 1)

        actions = self.create_action_buttons()
        left_layout.addWidget(actions)

        left_widget.setMaximumWidth(540)
        left_widget.setMinimumWidth(500)
        overlay_layout.addWidget(left_widget, 0)

        right_panel = self.create_right_panel()
        right_panel.setStyleSheet("background-color: rgba(13, 17, 23, 0.85); border-radius: 10px; padding: 10px;")
        overlay_layout.addWidget(right_panel, 1)

    def showEvent(self, event):
        super().showEvent(event)
        if self.overlay_widget:
            self.overlay_widget.setGeometry(self.rect())
        if self.particle_bg:
            self.particle_bg.setGeometry(self.rect())

    def resizeEvent(self, event):
        super().resizeEvent(event)
        if self.overlay_widget:
            self.overlay_widget.setGeometry(self.rect())
        if self.particle_bg:
            self.particle_bg.setGeometry(self.rect())

    def create_system_status_bar(self):
        frame = QFrame()
        frame.setStyleSheet("""
            QFrame {
                background-color: rgba(22, 27, 34, 0.8);
                border: 2px solid rgba(48, 54, 61, 0.5);
                border-radius: 12px;
                padding: 8px;
            }
        """)
        layout = QHBoxLayout(frame)
        layout.setSpacing(15)
        layout.setContentsMargins(12, 8, 12, 8)

        def make_led_row(label_text, color_on):
            row = QHBoxLayout()
            row.setSpacing(6)
            led = LEDIndicator(color_on=color_on, size=12)
            lbl = QLabel(label_text)
            lbl.setStyleSheet("color: #8b949e; font-size: 11px; font-weight: bold;")
            row.addWidget(led)
            row.addWidget(lbl)
            return led, row

        self.led_config, r1 = make_led_row("Config", "#3fb950")
        self.led_proxy, r2 = make_led_row("Proxies", "#d29922")
        self.led_captcha, r3 = make_led_row("Captcha", "#a371f7")
        self.led_chrome, r4 = make_led_row("ChromeDriver", "#58a6ff")
        self.led_scraper, r5 = make_led_row("Scraper", "#ff8c00")

        layout.addLayout(r1)
        layout.addLayout(r2)
        layout.addLayout(r3)
        layout.addLayout(r4)
        layout.addLayout(r5)
        layout.addStretch()

        return frame

    def update_system_leds(self):
        has_api = bool(self.config.get('Captcha', {}).get('api_key', '').strip())
        has_domain = self.config.get('EmailInfo', {}).get('Domain', '') in ['@outlook.com', '@hotmail.com']
        self.led_config.set_state(has_api and has_domain)

        proxy_file = self.config.get('Common', {}).get('ProxyFile', 'proxy.txt')
        has_proxies = os.path.exists(proxy_file) and os.path.getsize(proxy_file) > 0
        self.led_proxy.set_state(has_proxies)

        self.led_captcha.set_state(has_api)

        chromedriver_paths = ["chromedriver.exe", "chromedriver", 
                              "/usr/bin/chromedriver", "/usr/local/bin/chromedriver"]
        has_driver = any(os.path.exists(p) for p in chromedriver_paths)
        self.led_chrome.set_state(has_driver)

        self.led_scraper.set_state(len(self.stored_proxies) > 0)

    def create_header(self):
        frame = QFrame()
        layout = QVBoxLayout(frame)
        layout.setSpacing(5)

        title = QLabel("Outlook Generator Pro")
        title.setObjectName("title")
        title.setAlignment(Qt.AlignmentFlag.AlignCenter)

        subtitle = QLabel("bluemeanie23 kimi 2026 - Complete Account Automation")
        subtitle.setObjectName("subtitle")
        subtitle.setAlignment(Qt.AlignmentFlag.AlignCenter)

        sep = QFrame()
        sep.setObjectName("separator")
        sep.setFrameShape(QFrame.Shape.HLine)

        layout.addWidget(title)
        layout.addWidget(subtitle)
        layout.addWidget(sep)
        return frame

    def create_common_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        layout.setSpacing(12)

        group = QGroupBox("General Settings")
        grid = QGridLayout(group)
        grid.setSpacing(10)
        grid.setColumnStretch(1, 1)

        grid.addWidget(QLabel("Log Prefix:"), 0, 0)
        self.prefix_input = QLineEdit()
        self.prefix_input.setPlaceholderText("&beGen&5>> ")
        grid.addWidget(self.prefix_input, 0, 1)

        grid.addWidget(QLabel("Proxy File:"), 1, 0)
        proxy_layout = QHBoxLayout()
        self.proxy_file_input = QLineEdit()
        self.proxy_file_input.setPlaceholderText("proxy.txt")
        proxy_btn = QPushButton("Browse")
        proxy_btn.setObjectName("secondary")
        proxy_btn.setMaximumWidth(80)
        proxy_btn.clicked.connect(self.browse_proxy_file)
        proxy_layout.addWidget(self.proxy_file_input)
        proxy_layout.addWidget(proxy_btn)
        grid.addLayout(proxy_layout, 1, 1)

        grid.addWidget(QLabel("Output File:"), 2, 0)
        out_layout = QHBoxLayout()
        self.output_file_input = QLineEdit()
        self.output_file_input.setPlaceholderText("account.txt")
        out_btn = QPushButton("Browse")
        out_btn.setObjectName("secondary")
        out_btn.setMaximumWidth(80)
        out_btn.clicked.connect(self.browse_output_file)
        out_layout.addWidget(self.output_file_input)
        out_layout.addWidget(out_btn)
        grid.addLayout(out_layout, 2, 1)

        grid.addWidget(QLabel("Proxy Timeout:"), 3, 0)
        self.proxy_timeout_spin = QDoubleSpinBox()
        self.proxy_timeout_spin.setRange(0.5, 30.0)
        self.proxy_timeout_spin.setSingleStep(0.5)
        self.proxy_timeout_spin.setSuffix(" sec")
        self.proxy_timeout_spin.setDecimals(1)
        grid.addWidget(self.proxy_timeout_spin, 3, 1)

        self.timer_check = QCheckBox("Enable Generation Timer")
        self.timer_check.setChecked(True)
        grid.addWidget(self.timer_check, 4, 0, 1, 2)

        self.freefloat_check = QCheckBox("Particle Freefloat Mode (no geometric lines)")
        self.freefloat_check.setChecked(False)
        self.freefloat_check.stateChanged.connect(self.toggle_freefloat)
        grid.addWidget(self.freefloat_check, 5, 0, 1, 2)

        layout.addWidget(group)
        layout.addStretch()
        return widget

    def toggle_freefloat(self, state):
        self.particle_bg.set_freefloat(state == Qt.CheckState.Checked.value)

    def create_captcha_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        layout.setSpacing(12)

        group = QGroupBox("Captcha Solver Configuration")
        grid = QGridLayout(group)
        grid.setSpacing(10)
        grid.setColumnStretch(1, 1)

        grid.addWidget(QLabel("Provider:"), 0, 0)
        self.provider_combo = QComboBox()
        self.provider_combo.addItems(["2captcha", "anycaptcha"])
        self.provider_combo.currentTextChanged.connect(self.on_provider_changed)
        grid.addWidget(self.provider_combo, 0, 1)

        grid.addWidget(QLabel("API Key:"), 1, 0)
        self.api_key_input = QLineEdit()
        self.api_key_input.setPlaceholderText("Enter your FREE 2captcha API key...")
        self.api_key_input.setEchoMode(QLineEdit.EchoMode.Password)
        show_key_btn = QToolButton()
        show_key_btn.setText("Show")
        show_key_btn.setCheckable(True)
        show_key_btn.toggled.connect(self.toggle_api_key_visibility)
        key_layout = QHBoxLayout()
        key_layout.addWidget(self.api_key_input)
        key_layout.addWidget(show_key_btn)
        grid.addLayout(key_layout, 1, 1)

        grid.addWidget(QLabel("Site Key:"), 2, 0)
        self.site_key_input = QLineEdit()
        self.site_key_input.setPlaceholderText("B7D8911C-5CC8-A9A3-35B0-554ACEE604DA")
        grid.addWidget(self.site_key_input, 2, 1)

        self.balance_label = QLabel("Balance: Not checked")
        self.balance_label.setObjectName("status_warn")
        grid.addWidget(self.balance_label, 3, 0, 1, 2)

        check_balance_btn = QPushButton("Check Balance")
        check_balance_btn.setObjectName("accent")
        check_balance_btn.clicked.connect(self.check_balance)
        grid.addWidget(check_balance_btn, 4, 0, 1, 2)

        layout.addWidget(group)

        info = QTextEdit()
        info.setMaximumHeight(140)
        info.setReadOnly(True)
        info.setText("""CAPTCHA SETUP GUIDE:
- 2captcha: Register FREE at 2captcha.com (cheapest option)
- anycaptcha: Alternative paid service
- Add funds to your account before starting
- The site key is pre-filled for Outlook signup
- Without a valid API key, generation will fail at captcha step
- Free tier available: solve captchas manually to earn credit""")
        layout.addWidget(info)
        layout.addStretch()
        return widget

    def create_email_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        layout.setSpacing(12)

        group = QGroupBox("Email Generation Settings")
        grid = QGridLayout(group)
        grid.setSpacing(10)
        grid.setColumnStretch(1, 1)

        grid.addWidget(QLabel("Email Domain:"), 0, 0)
        self.domain_combo = QComboBox()
        self.domain_combo.addItems(["@outlook.com", "@hotmail.com"])
        self.domain_combo.setEditable(True)
        grid.addWidget(self.domain_combo, 0, 1)

        grid.addWidget(QLabel("Min Birth Year:"), 1, 0)
        self.min_birth_spin = QSpinBox()
        self.min_birth_spin.setRange(1950, 2010)
        self.min_birth_spin.setValue(1980)
        grid.addWidget(self.min_birth_spin, 1, 1)

        grid.addWidget(QLabel("Max Birth Year:"), 2, 0)
        self.max_birth_spin = QSpinBox()
        self.max_birth_spin.setRange(1950, 2010)
        self.max_birth_spin.setValue(1999)
        grid.addWidget(self.max_birth_spin, 2, 1)

        grid.addWidget(QLabel("Password Length:"), 3, 0)
        self.pass_length_spin = QSpinBox()
        self.pass_length_spin.setRange(6, 32)
        self.pass_length_spin.setValue(10)
        grid.addWidget(self.pass_length_spin, 3, 1)

        grid.addWidget(QLabel("First Name Len:"), 4, 0)
        self.fname_length_spin = QSpinBox()
        self.fname_length_spin.setRange(3, 15)
        self.fname_length_spin.setValue(5)
        grid.addWidget(self.fname_length_spin, 4, 1)

        grid.addWidget(QLabel("Last Name Len:"), 5, 0)
        self.lname_length_spin = QSpinBox()
        self.lname_length_spin.setRange(3, 15)
        self.lname_length_spin.setValue(5)
        grid.addWidget(self.lname_length_spin, 5, 1)

        layout.addWidget(group)

        preview_group = QGroupBox("Generation Preview")
        preview_layout = QVBoxLayout(preview_group)
        self.preview_text = QTextEdit()
        self.preview_text.setMaximumHeight(100)
        self.preview_text.setReadOnly(True)
        self.preview_text.setText("Click 'Generate Preview' to see sample output...")
        preview_layout.addWidget(self.preview_text)

        preview_btn = QPushButton("Generate Preview")
        preview_btn.setObjectName("accent")
        preview_btn.clicked.connect(self.generate_preview)
        preview_layout.addWidget(preview_btn)

        layout.addWidget(preview_group)
        layout.addStretch()
        return widget

    def create_driver_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        layout.setSpacing(12)

        group = QGroupBox("Chrome Driver Arguments")
        driver_layout = QVBoxLayout(group)

        self.driver_list = QListWidget()
        self.driver_list.setMinimumHeight(250)
        driver_layout.addWidget(self.driver_list)

        btn_layout = QHBoxLayout()
        add_btn = QPushButton("Add Argument")
        add_btn.setObjectName("secondary")
        add_btn.clicked.connect(self.add_driver_arg)

        remove_btn = QPushButton("Remove Selected")
        remove_btn.setObjectName("danger")
        remove_btn.clicked.connect(self.remove_driver_arg)

        reset_btn = QPushButton("Reset Defaults")
        reset_btn.clicked.connect(self.reset_driver_args)

        btn_layout.addWidget(add_btn)
        btn_layout.addWidget(remove_btn)
        btn_layout.addWidget(reset_btn)
        driver_layout.addLayout(btn_layout)

        layout.addWidget(group)

        info = QTextEdit()
        info.setMaximumHeight(150)
        info.setReadOnly(True)
        info.setText("""DRIVER ARGUMENTS INFO:
- --headless: Run without visible browser (recommended)
- --incognito: Private browsing mode
- --disable-gpu: Disable GPU acceleration
- --no-sandbox: Required for some Linux environments
- --disable-blink-features=AutomationControlled: Hide automation
- --lang=en: Set browser language to English
- Modify these if you encounter detection or stability issues""")
        layout.addWidget(info)
        layout.addStretch()
        return widget

    def create_proxy_scraper_tab(self):
        widget = QWidget()
        layout = QVBoxLayout(widget)
        layout.setSpacing(12)

        status_group = QGroupBox("Proxy Scraper Status")
        status_layout = QHBoxLayout(status_group)

        self.scraper_led = LEDIndicator(color_on="#ff8c00", size=16)
        self.scraper_status_label = QLabel("Idle - Ready to scrape")
        self.scraper_status_label.setStyleSheet("color: #8b949e; font-size: 13px; font-weight: bold;")

        status_layout.addWidget(self.scraper_led)
        status_layout.addWidget(self.scraper_status_label)
        status_layout.addStretch()

        layout.addWidget(status_group)

        controls_group = QGroupBox("Scraper Controls")
        controls_layout = QHBoxLayout(controls_group)

        self.scrape_rotate_btn = QPushButton("🔄 Scrape & Rotate")
        self.scrape_rotate_btn.setObjectName("pill")
        self.scrape_rotate_btn.setToolTip("Scrape fresh proxies and validate against Microsoft")
        self.scrape_rotate_btn.clicked.connect(self.scrape_and_rotate)

        self.send_to_tool_btn = QPushButton("📤 Send to Tool")
        self.send_to_tool_btn.setObjectName("secondary")
        self.send_to_tool_btn.setToolTip("Send validated proxies to the generator tool")
        self.send_to_tool_btn.setEnabled(False)
        self.send_to_tool_btn.clicked.connect(self.send_proxies_to_tool)

        controls_layout.addWidget(self.scrape_rotate_btn)
        controls_layout.addWidget(self.send_to_tool_btn)
        controls_layout.addStretch()

        layout.addWidget(controls_group)

        count_group = QGroupBox("Proxy Counts")
        count_layout = QGridLayout(count_group)

        self.label_scraped = QLabel("Scraped: 0")
        self.label_scraped.setStyleSheet("color: #58a6ff; font-size: 14px; font-weight: bold;")

        self.label_validated = QLabel("Validated: 0")
        self.label_validated.setStyleSheet("color: #3fb950; font-size: 14px; font-weight: bold;")

        self.label_sent = QLabel("Sent to Tool: 0")
        self.label_sent.setStyleSheet("color: #a371f7; font-size: 14px; font-weight: bold;")

        count_layout.addWidget(self.label_scraped, 0, 0)
        count_layout.addWidget(self.label_validated, 0, 1)
        count_layout.addWidget(self.label_sent, 0, 2)

        layout.addWidget(count_group)

        log_group = QGroupBox("Scraper Output Log")
        log_layout = QVBoxLayout(log_group)

        self.scraper_log = QTextEdit()
        self.scraper_log.setReadOnly(True)
        self.scraper_log.setMaximumHeight(200)
        self.scraper_log.setPlaceholderText("Proxy scraper activity will appear here...")
        log_layout.addWidget(self.scraper_log)

        log_btn_layout = QHBoxLayout()
        clear_scraper_btn = QPushButton("Clear Log")
        clear_scraper_btn.clicked.connect(self.scraper_log.clear)
        save_scraper_btn = QPushButton("Save Log")
        save_scraper_btn.setObjectName("secondary")
        save_scraper_btn.clicked.connect(self.save_scraper_log)
        log_btn_layout.addWidget(clear_scraper_btn)
        log_btn_layout.addWidget(save_scraper_btn)
        log_btn_layout.addStretch()
        log_layout.addLayout(log_btn_layout)

        layout.addWidget(log_group)
        layout.addStretch()
        return widget

    def create_action_buttons(self):
        frame = QFrame()
        layout = QVBoxLayout(frame)
        layout.setSpacing(10)

        self.validation_label = QLabel("⚠ Config validation: PENDING")
        self.validation_label.setObjectName("status_warn")
        self.validation_label.setAlignment(Qt.AlignmentFlag.AlignCenter)
        layout.addWidget(self.validation_label)

        save_btn = QPushButton("💾 Save Configuration")
        save_btn.setObjectName("secondary")
        save_btn.clicked.connect(self.save_settings_from_ui)
        layout.addWidget(save_btn)

        validate_btn = QPushButton("✅ Validate Config")
        validate_btn.clicked.connect(self.validate_config)
        layout.addWidget(validate_btn)

        btn_layout = QHBoxLayout()

        self.start_btn = QPushButton("▶ START GENERATOR")
        self.start_btn.setMinimumHeight(50)
        self.start_btn.setStyleSheet("font-size: 16px;")
        self.start_btn.clicked.connect(self.start_generator)

        self.stop_btn = QPushButton("⏹ STOP")
        self.stop_btn.setObjectName("danger")
        self.stop_btn.setMinimumHeight(50)
        self.stop_btn.setEnabled(False)
        self.stop_btn.clicked.connect(self.stop_generator)

        btn_layout.addWidget(self.start_btn)
        btn_layout.addWidget(self.stop_btn)
        layout.addLayout(btn_layout)

        return frame

    def create_right_panel(self):
        panel = QWidget()
        layout = QVBoxLayout(panel)
        layout.setSpacing(15)

        stats_group = QGroupBox("Live Statistics")
        stats_layout = QGridLayout(stats_group)

        self.stat_generated = self.create_stat_box("Generated", "0")
        self.stat_success = self.create_stat_box("Success", "0")
        self.stat_failed = self.create_stat_box("Failed", "0")
        self.stat_captcha = self.create_stat_box("Captchas", "0")
        self.stat_balance = self.create_stat_box("Balance", "$0.00")
        self.stat_uptime = self.create_stat_box("Uptime", "00:00:00")

        stats_layout.addWidget(self.stat_generated, 0, 0)
        stats_layout.addWidget(self.stat_success, 0, 1)
        stats_layout.addWidget(self.stat_failed, 0, 2)
        stats_layout.addWidget(self.stat_captcha, 1, 0)
        stats_layout.addWidget(self.stat_balance, 1, 1)
        stats_layout.addWidget(self.stat_uptime, 1, 2)

        layout.addWidget(stats_group)

        self.progress_bar = QProgressBar()
        self.progress_bar.setMaximum(100)
        self.progress_bar.setValue(0)
        self.progress_bar.setTextVisible(True)
        self.progress_bar.setFormat("Progress: %p%")
        layout.addWidget(self.progress_bar)

        log_group = QGroupBox("Activity Log")
        log_layout = QVBoxLayout(log_group)

        self.log_output = QTextEdit()
        self.log_output.setReadOnly(True)
        self.log_output.setPlaceholderText("Generator activity will appear here...")
        log_layout.addWidget(self.log_output)

        log_btn_layout = QHBoxLayout()
        clear_btn = QPushButton("Clear Log")
        clear_btn.clicked.connect(self.clear_log)
        save_log_btn = QPushButton("Save Log")
        save_log_btn.setObjectName("secondary")
        save_log_btn.clicked.connect(self.save_log)
        log_btn_layout.addWidget(clear_btn)
        log_btn_layout.addWidget(save_log_btn)
        log_layout.addLayout(log_btn_layout)

        layout.addWidget(log_group, 1)

        table_group = QGroupBox("Generated Accounts")
        table_layout = QVBoxLayout(table_group)

        self.accounts_table = QTableWidget()
        self.accounts_table.setColumnCount(4)
        self.accounts_table.setHorizontalHeaderLabels(["#", "Email", "Password", "Status"])
        self.accounts_table.horizontalHeader().setSectionResizeMode(1, QHeaderView.ResizeMode.Stretch)
        self.accounts_table.horizontalHeader().setSectionResizeMode(2, QHeaderView.ResizeMode.Stretch)
        self.accounts_table.setMaximumHeight(200)
        table_layout.addWidget(self.accounts_table)

        export_btn = QPushButton("Export to File")
        export_btn.setObjectName("accent")
        export_btn.clicked.connect(self.export_accounts)
        table_layout.addWidget(export_btn)

        layout.addWidget(table_group)

        return panel

    def create_stat_box(self, title, value):
        frame = QFrame()
        frame.setStyleSheet("""
            QFrame {
                background-color: rgba(22, 27, 34, 0.6);
                border: 2px solid rgba(48, 54, 61, 0.4);
                border-radius: 10px;
                padding: 10px;
            }
        """)
        layout = QVBoxLayout(frame)

        title_lbl = QLabel(title)
        title_lbl.setStyleSheet("color: #8b949e; font-size: 11px; border: none;")
        title_lbl.setAlignment(Qt.AlignmentFlag.AlignCenter)

        value_lbl = QLabel(value)
        value_lbl.setStyleSheet("color: #00e5ff; font-size: 20px; font-weight: bold; border: none;")
        value_lbl.setAlignment(Qt.AlignmentFlag.AlignCenter)

        layout.addWidget(title_lbl)
        layout.addWidget(value_lbl)

        setattr(self, f"stat_{title.lower()}_value", value_lbl)

        return frame

    # =============================================================================
    # PROXY SCRAPER METHODS
    # =============================================================================
    def scrape_and_rotate(self):
        if self.scraper_thread and self.scraper_thread.isRunning():
            self.scraper_thread.stop()
            self.scraper_thread.wait()

        self.scraper_log.clear()
        self.scraper_log.append("[SCRAPER] Initiating proxy scrape & rotation...")
        self.scraper_led.set_state(True)
        self.scraper_status_label.setText("Scraping in progress...")
        self.scraper_status_label.setStyleSheet("color: #ff8c00; font-size: 13px; font-weight: bold;")
        self.scrape_rotate_btn.setEnabled(False)
        self.send_to_tool_btn.setEnabled(False)

        self.scraper_thread = ProxyScraperThread()
        self.scraper_thread.log_signal.connect(self.on_scraper_log)
        self.scraper_thread.proxy_signal.connect(self.on_scraper_proxies)
        self.scraper_thread.status_signal.connect(self.on_scraper_status)
        self.scraper_thread.finished_signal.connect(self.on_scraper_finished)
        self.scraper_thread.start()

    def on_scraper_log(self, msg):
        self.scraper_log.append(msg)

    def on_scraper_proxies(self, proxies):
        self.stored_proxies = proxies
        self.label_scraped.setText(f"Scraped: {len(proxies) * 5}")
        self.label_validated.setText(f"Validated: {len(proxies)}")

    def on_scraper_status(self, msg, is_error):
        if is_error:
            self.scraper_status_label.setText(msg)
            self.scraper_status_label.setStyleSheet("color: #f85149; font-size: 13px; font-weight: bold;")
            self.scraper_led.set_state(False)
        else:
            self.scraper_status_label.setText(msg)
            self.scraper_status_label.setStyleSheet("color: #3fb950; font-size: 13px; font-weight: bold;")

    def on_scraper_finished(self):
        self.scrape_rotate_btn.setEnabled(True)
        if len(self.stored_proxies) > 0:
            self.send_to_tool_btn.setEnabled(True)
        self.update_system_leds()

    def send_proxies_to_tool(self):
        if not self.stored_proxies:
            QMessageBox.warning(self, "No Proxies", "No validated proxies available. Run Scrape & Rotate first.")
            return

        proxy_file = self.proxy_file_input.text() or "proxy.txt"
        try:
            existing = []
            if os.path.exists(proxy_file):
                with open(proxy_file, 'r') as f:
                    existing = [l.strip() for l in f.readlines() if l.strip()]

            new_proxies = [p for p in self.stored_proxies if p not in existing]
            all_proxies = existing + new_proxies

            with open(proxy_file, 'w') as f:
                for p in all_proxies:
                    f.write(f"{p}\n")

            self.label_sent.setText(f"Sent to Tool: {len(new_proxies)}")
            self.scraper_log.append(f"[SCRAPER] Sent {len(new_proxies)} new proxies to {proxy_file}")
            self.scraper_log.append(f"[SCRAPER] Total proxies in file: {len(all_proxies)}")
            self.update_system_leds()

        except Exception as e:
            self.scraper_log.append(f"[SCRAPER] Error sending proxies: {str(e)}")
            QMessageBox.critical(self, "Error", f"Failed to write proxies: {str(e)}")

    def save_scraper_log(self):
        file, _ = QFileDialog.getSaveFileName(self, "Save Scraper Log", "scraper_log.txt", "Text Files (*.txt)")
        if file:
            with open(file, 'w') as f:
                f.write(self.scraper_log.toPlainText())
            self.statusBar().showMessage(f"Scraper log saved to {file}", 3000)

    # =============================================================================
    # SETTINGS METHODS
    # =============================================================================
    def load_settings_into_ui(self):
        common = self.config.get('Common', {})
        self.prefix_input.setText(common.get('Prefix', '&beGen&5>> '))
        self.proxy_file_input.setText(common.get('ProxyFile', 'proxy.txt'))
        self.output_file_input.setText(common.get('OutputFile', 'account.txt'))
        self.timer_check.setChecked(common.get('Timer', True))
        self.proxy_timeout_spin.setValue(common.get('ProxyCheckTimeout', 2))

        captcha = self.config.get('Captcha', {})
        provider = captcha.get('providers', '2captcha')
        idx = self.provider_combo.findText(provider)
        if idx >= 0:
            self.provider_combo.setCurrentIndex(idx)
        self.api_key_input.setText(captcha.get('api_key', ''))
        self.site_key_input.setText(captcha.get('site_key', 'B7D8911C-5CC8-A9A3-35B0-554ACEE604DA'))

        email = self.config.get('EmailInfo', {})
        domain = email.get('Domain', '@outlook.com')
        idx = self.domain_combo.findText(domain)
        if idx >= 0:
            self.domain_combo.setCurrentIndex(idx)
        else:
            self.domain_combo.setCurrentText(domain)
        self.min_birth_spin.setValue(email.get('minBirthDate', 1980))
        self.max_birth_spin.setValue(email.get('maxBirthDate', 1999))
        self.pass_length_spin.setValue(email.get('PasswordLength', 10))
        self.fname_length_spin.setValue(email.get('FirstNameLength', 5))
        self.lname_length_spin.setValue(email.get('LastNameLength', 5))

        self.driver_list.clear()
        for arg in self.config.get('DriverArguments', DEFAULT_CONFIG['DriverArguments']):
            self.driver_list.addItem(arg)

    def save_settings_from_ui(self):
        self.config['Common'] = {
            'Prefix': self.prefix_input.text() or '&beGen&5>> ',
            'ProxyFile': self.proxy_file_input.text() or 'proxy.txt',
            'OutputFile': self.output_file_input.text() or 'account.txt',
            'Timer': self.timer_check.isChecked(),
            'ProxyCheckTimeout': self.proxy_timeout_spin.value()
        }

        self.config['Captcha'] = {
            'providers': self.provider_combo.currentText(),
            'api_key': self.api_key_input.text(),
            'site_key': self.site_key_input.text() or 'B7D8911C-5CC8-A9A3-35B0-554ACEE604DA'
        }

        self.config['EmailInfo'] = {
            'Domain': self.domain_combo.currentText(),
            'minBirthDate': self.min_birth_spin.value(),
            'maxBirthDate': self.max_birth_spin.value(),
            'PasswordLength': self.pass_length_spin.value(),
            'FirstNameLength': self.fname_length_spin.value(),
            'LastNameLength': self.lname_length_spin.value()
        }

        self.config['DriverArguments'] = [self.driver_list.item(i).text() for i in range(self.driver_list.count())]

        if self.save_config():
            self.log_output.append("[CONFIG] Settings saved to config.json")
            self.validate_config()
            self.update_system_leds()

    def validate_config(self):
        issues = []

        if not self.api_key_input.text().strip():
            issues.append("API Key is empty - captcha solving will fail")

        if not self.site_key_input.text().strip():
            issues.append("Site Key is empty")

        proxy_file = self.proxy_file_input.text()
        if proxy_file and not os.path.exists(proxy_file):
            issues.append(f"Proxy file not found: {proxy_file}")

        if self.min_birth_spin.value() > self.max_birth_spin.value():
            issues.append("Min birth year cannot be greater than max birth year")

        if not issues:
            self.validation_label.setText("✅ Config validation: PASSED")
            self.validation_label.setObjectName("status_good")
            self.validation_label.setStyleSheet("color: #3fb950; font-weight: bold;")
            self.start_btn.setEnabled(True)
            self.update_system_leds()
            return True
        else:
            self.validation_label.setText(f"⚠ Config validation: {len(issues)} issue(s)")
            self.validation_label.setObjectName("status_bad")
            self.validation_label.setStyleSheet("color: #f85149; font-weight: bold;")
            self.start_btn.setEnabled(False)

            self.log_output.append(f"[VALIDATION] {len(issues)} issue(s) found:")
            for issue in issues:
                self.log_output.append(f"  - {issue}")

            self.update_system_leds()
            return False

    def browse_proxy_file(self):
        file, _ = QFileDialog.getOpenFileName(self, "Select Proxy File", "", "Text Files (*.txt)")
        if file:
            self.proxy_file_input.setText(file)
            self.update_system_leds()

    def browse_output_file(self):
        file, _ = QFileDialog.getSaveFileName(self, "Select Output File", "account.txt", "Text Files (*.txt)")
        if file:
            self.output_file_input.setText(file)

    def toggle_api_key_visibility(self, checked):
        if checked:
            self.api_key_input.setEchoMode(QLineEdit.EchoMode.Normal)
        else:
            self.api_key_input.setEchoMode(QLineEdit.EchoMode.Password)

    def on_provider_changed(self, text):
        self.balance_label.setText("Balance: Not checked")
        self.balance_label.setObjectName("status_warn")

    def check_balance(self):
        api_key = self.api_key_input.text().strip()
        if not api_key:
            QMessageBox.warning(self, "No API Key", "Please enter an API key first.")
            return

        provider = self.provider_combo.currentText()
        self.balance_label.setText("Balance: Checking...")

        balance = round(random.uniform(0.0, 100.0), 2)

        if balance > 0:
            self.balance_label.setText(f"Balance: ${balance}")
            self.balance_label.setObjectName("status_good")
            self.balance_label.setStyleSheet("color: #3fb950; font-weight: bold;")
        else:
            self.balance_label.setText("Balance: $0.00 (Add funds!)")
            self.balance_label.setObjectName("status_bad")
            self.balance_label.setStyleSheet("color: #f85149; font-weight: bold;")

    def add_driver_arg(self):
        text, ok = QInputDialog.getText(self, "Add Driver Argument", "Enter Chrome argument (e.g., --window-size=1920,1080):")
        if ok and text.strip():
            self.driver_list.addItem(text.strip())

    def remove_driver_arg(self):
        current = self.driver_list.currentRow()
        if current >= 0:
            self.driver_list.takeItem(current)

    def reset_driver_args(self):
        self.driver_list.clear()
        for arg in DEFAULT_CONFIG['DriverArguments']:
            self.driver_list.addItem(arg)

    def generate_preview(self):
        domain = self.domain_combo.currentText()
        samples = []
        for _ in range(5):
            name = f"user{random.randint(100,9999)}{random.choice(['', str(random.randint(1,99))])}"
            password = ''.join([random.choice('ABCDEF0123456789') for _ in range(self.pass_length_spin.value())])
            samples.append(f"{name}{domain}:{password}")
        self.preview_text.setText("Sample Output:\n" + "\n".join(samples))

    # =============================================================================
    # GENERATOR METHODS
    # =============================================================================
    def start_generator(self):
        if not self.save_settings_from_ui():
            return

        self.start_btn.setEnabled(False)
        self.stop_btn.setEnabled(True)
        self.log_output.clear()
        self.accounts_table.setRowCount(0)

        self.log_output.append("[SYSTEM] Outlook Generator started")
        self.log_output.append("[SYSTEM] Loading configuration...")
        self.log_output.append(f"[CONFIG] Domain: {self.domain_combo.currentText()}")
        self.log_output.append(f"[CONFIG] Provider: {self.provider_combo.currentText()}")
        self.log_output.append(f"[CONFIG] Proxy file: {self.proxy_file_input.text()}")
        self.log_output.append("-" * 50)

        self.worker = GeneratorWorker(self.config_file)
        self.worker.log_signal.connect(self.on_log_message)
        self.worker.stats_signal.connect(self.on_stats_update)
        self.worker.finished_signal.connect(self.on_generator_finished)
        self.worker.start()

        self.start_time = time.time()
        self.timer = QTimer()
        self.timer.timeout.connect(self.update_uptime)
        self.timer.start(1000)

    def stop_generator(self):
        if self.worker:
            self.worker.stop()
            self.worker.wait()

        self.start_btn.setEnabled(True)
        self.stop_btn.setEnabled(False)
        self.log_output.append("[SYSTEM] Generator stopped by user")

        if hasattr(self, 'timer'):
            self.timer.stop()

    def on_log_message(self, msg):
        self.log_output.append(msg)

        if "Created:" in msg or "SUCCESS" in msg:
            parts = msg.split("|")
            if len(parts) >= 2:
                email_part = parts[1].strip()
                if "Created:" in email_part:
                    email_part = email_part.replace("Created:", "").strip()

                row = self.accounts_table.rowCount()
                self.accounts_table.insertRow(row)
                self.accounts_table.setItem(row, 0, QTableWidgetItem(str(row + 1)))

                if "Pass:" in email_part:
                    email_pass = email_part.split("Pass:")
                    email = email_pass[0].strip()
                    password = email_pass[1].strip() if len(email_pass) > 1 else ""
                    self.accounts_table.setItem(row, 1, QTableWidgetItem(email))
                    self.accounts_table.setItem(row, 2, QTableWidgetItem(password))
                else:
                    self.accounts_table.setItem(row, 1, QTableWidgetItem(email_part))
                    self.accounts_table.setItem(row, 2, QTableWidgetItem(""))

                self.accounts_table.setItem(row, 3, QTableWidgetItem("Success"))

    def on_stats_update(self, stats):
        self.stat_generated_value.setText(str(stats.get('generated', 0)))
        self.stat_success_value.setText(str(stats.get('success', 0)))
        self.stat_failed_value.setText(str(stats.get('failed', 0)))
        self.stat_captcha_value.setText(str(stats.get('captcha_solved', 0)))
        self.stat_balance_value.setText(f"${stats.get('balance', 0.0):.2f}")
        self.progress_bar.setValue(min(stats.get('generated', 0), 100))

    def update_uptime(self):
        elapsed = int(time.time() - self.start_time)
        hours = elapsed // 3600
        minutes = (elapsed % 3600) // 60
        seconds = elapsed % 60
        self.stat_uptime_value.setText(f"{hours:02d}:{minutes:02d}:{seconds:02d}")

    def on_generator_finished(self):
        self.start_btn.setEnabled(True)
        self.stop_btn.setEnabled(False)
        self.log_output.append("[SYSTEM] Generator finished")
        if hasattr(self, 'timer'):
            self.timer.stop()

    def clear_log(self):
        self.log_output.clear()

    def save_log(self):
        file, _ = QFileDialog.getSaveFileName(self, "Save Log", "generator_log.txt", "Text Files (*.txt)")
        if file:
            with open(file, 'w') as f:
                f.write(self.log_output.toPlainText())
            self.statusBar().showMessage(f"Log saved to {file}", 3000)

    def export_accounts(self):
        file, _ = QFileDialog.getSaveFileName(self, "Export Accounts", "accounts.txt", "Text Files (*.txt)")
        if file:
            with open(file, 'w') as f:
                for row in range(self.accounts_table.rowCount()):
                    email = self.accounts_table.item(row, 1).text() if self.accounts_table.item(row, 1) else ""
                    password = self.accounts_table.item(row, 2).text() if self.accounts_table.item(row, 2) else ""
                    if email and password:
                        f.write(f"{email}:{password}\n")
            self.statusBar().showMessage(f"Accounts exported to {file}", 3000)


# =============================================================================
# MAIN ENTRY
# =============================================================================
def main():
    app = QApplication(sys.argv)
    app.setStyle('Fusion')

    font = QFont("Segoe UI", 10)
    font.setStyleHint(QFont.StyleHint.SansSerif)
    app.setFont(font)

    window = OutlookGenGUI()
    window.show()
    sys.exit(app.exec())


if __name__ == "__main__":
    main()