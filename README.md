# Aplicación del Cálculo Integral en la Estimación de Posición y Velocidad para Sistemas de Navegación Inercial (INS)

**Universidad Mariano Gálvez de Guatemala**  
**Facultad de Ingeniería en Sistemas**  
**Campus Jutiapa**  
**Curso:** Cálculo II / Proyecto Final  
**Fecha de entrega:** Octubre 2026  

---

## 👥 Integrantes del Equipo

| Nombre Completo | Carné |
| :--- | :---: |
| Juan Felipe Sandoval Recinos | 0905-25-10985 |
| José Javier Alay Méndez | 0905-25-11899 |
| Deiby Estuardo Valdez Estrada | 0905-25-14041 |
| William Noé Monzón López | 0905-25-16590 |
| Josué David Martínez Ortíz | 0905-25-21930 |

---

## 📌 1. Planteamiento y Descripción del Problema

### Pregunta de Investigación
> *¿Cómo estimar con precisión la velocidad y la posición en tiempo real de un vehículo autónomo mediante la aplicación de la doble integración cuando ocurre una pérdida de señal GPS?*

### Contexto del Problema
En la robótica y el transporte autónomo actual, el Sistema de Posicionamiento Global (GPS) es el sensor primario de geolocalización. Sin embargo, en entornos confinados (túneles viales, pasos a desnivel subterráneos o cañones urbanos), la señal satelital se bloquea o sufre de interferencia severa. Una desconexión no mitigada puede ocasionar colisiones, desvíos críticos o pérdidas de control vehicular.

Como contingencia crítica, entra en operación el **Sistema de Navegación Inercial (INS)** soportado por una **Unidad de Medición Inercial (IMU)** compuesta por acelerómetros y giroscopios triaxiales. Al ser un sistema 100% autónomo (no emite ni recibe señales externas), requiere reconstruir la cinemática a través del cálculo integral y multivariable.

---

## 📐 2. Formulación y Desarrollo Matemático

### 2.1 Condiciones Iniciales del Caso Práctico
* Longitud del túnel: $150\text{ m}$
* Posición inicial al entrar al túnel: $s(0) = s_0 = 0\text{ m}$
* Velocidad inicial registrada: $v(0) = v_0 = 15\text{ m/s}$
* Instante de pérdida de señal satelital: $t = 0\text{ s}$
* Tasa de rotación angular registrada por giroscopio: $\omega(t) = 0.02t$
* Perfil de desaceleración registrado por acelerómetro: $a(t) = -0.5t \cdot e^{-0.1t}$

---

### 2.2 Orientación Angular $\theta(t)$ (Integración Directa)
Integrando la velocidad angular respecto al tiempo con $\theta_0 = 0$:
$$\theta(t) = \theta_0 + \int \omega(t) \, dt = \int 0.02t \, dt = 0.01t^2$$

Evaluando en $t = 5\text{ s}$:
$$\theta(5) = 0.01(5)^2 = 0.25\text{ rad}$$
$$\theta(5) = 0.25 \cdot \left(\frac{180}{\pi}\right) \approx +14.32^\circ$$

---

### 2.3 Obtención de la Velocidad $v(t)$ (Integración por Partes)
La velocidad es la antiderivada de la aceleración:
$$v(t) = v_0 + \int a(t) \, dt = 15 + \int (-0.5t \cdot e^{-0.1t}) \, dt$$

Aplicando el método de **integración por partes** ($\int u \, dv = uv - \int v \, du$):
* $u = -0.5t \implies du = -0.5 \, dt$
* $dv = e^{-0.1t} \, dt \implies v = \frac{1}{-0.1}e^{-0.1t} = -10e^{-0.1t}$

Desarrollo:
$$\int (-0.5t \cdot e^{-0.1t}) \, dt = (-0.5t)(-10e^{-0.1t}) - \int (-10e^{-0.1t})(-0.5) \, dt$$
$$= 5t \cdot e^{-0.1t} - 5 \int e^{-0.1t} \, dt$$
$$= 5t \cdot e^{-0.1t} + 50e^{-0.1t} + C$$

Determinación de la constante $C$ evaluando en $t = 0$ con $v(0) = 15$:
$$15 = 15 + [5(0)e^0 + 50e^0] + C \implies 15 = 15 + 50 + C \implies C = -50$$

Ecuación de velocidad:
$$v(t) = 5t \cdot e^{-0.1t} + 50e^{-0.1t} - 35$$

Evaluando en $t = 5\text{ s}$:
$$v(5) = 5(5)e^{-0.5} + 50e^{-0.5} - 35 = 75(0.6065) - 35 \approx 10.49\text{ m/s}$$

---

### 2.4 Obtención de la Posición $s(t)$ (Segunda Integración)
La posición corresponde a la integral de la función de velocidad:
$$s(t) = s_0 + \int v(t) \, dt = 0 + \int \left(5t \cdot e^{-0.1t} + 50e^{-0.1t} - 35\right) \, dt$$

Separando en integrales individuales:
1. $\int 5t \cdot e^{-0.1t} \, dt$ (por partes con $u = 5t$, $dv = e^{-0.1t} dt$):
   $$\int 5t \cdot e^{-0.1t} \, dt = -50t \cdot e^{-0.1t} - 500e^{-0.1t}$$
2. $\int 50e^{-0.1t} \, dt = -500e^{-0.1t}$
3. $\int (-35) \, dt = -35t$

Sumando los términos:
$$s(t) = -50t \cdot e^{-0.1t} - 1000e^{-0.1t} - 35t + C$$

Determinación de la constante $C$ con condición inicial $s(0) = 0$:
$$0 = -50(0) - 1000(1) - 35(0) + C \implies C = 1000$$

Ecuación general de desplazamiento:
$$s(t) = -50t \cdot e^{-0.1t} - 1000e^{-0.1t} - 35t + 1000$$

Evaluando en $t = 5\text{ s}$:
$$s(5) = -250(0.6065) - 1000(0.6065) - 175 + 1000 \approx 66.88\text{ m}$$

---

### 2.5 Teorema del Valor Promedio para la Velocidad
Para determinar la velocidad media continua del vehículo en el intervalo $[0, 5]\text{ s}$:
$$V_{\text{prom}} = \frac{1}{b - a} \int_{a}^{b} v(t) \, dt = \frac{1}{5 - 0} \left[s(5) - s(0)\right]$$
$$V_{\text{prom}} = \frac{1}{5} (66.875 - 0) \approx 13.375\text{ m/s}$$

---

### 2.6 Análisis de Propagación de Error y Deriva con Derivadas Parciales
Los sensores inerciales presentan sesgos instrumentales ($\epsilon_a$ en aceleración y $\epsilon_\theta$ en ángulo) que se integran y amplifican cuadráticamente con el tiempo (*drift* inercial):

1. **Sensibilidad al error del acelerómetro ($\epsilon_a$):**
   $$E_{\text{pos}} = \frac{1}{2}\epsilon_a t^2 \implies \frac{\partial E_{\text{pos}}}{\partial \epsilon_a} = \frac{1}{2}t^2$$
   Para $t = 5\text{ s}$:
   $$\frac{\partial E_{\text{pos}}}{\partial \epsilon_a} = \frac{1}{2}(5)^2 = 12.5\text{ s}^2$$
   *Con un sesgo de $\epsilon_a = 2.0\text{ m/s}^2$, la desviación de posición es:*
   $$\Delta s_a = 12.5 \cdot 2.0 = 25.0\text{ m}$$

2. **Sensibilidad al error de orientación del giroscopio ($\epsilon_\theta$):**
   $$E_{\text{pos}} = \frac{1}{2} V_{\text{prom}} \cdot \epsilon_\theta t^2 \implies \frac{\partial E_{\text{pos}}}{\partial \epsilon_\theta} = \frac{1}{2} V_{\text{prom}} t^2$$
   Para $t = 5\text{ s}$ y $V_{\text{prom}} = 13.375\text{ m/s}$:
   $$\frac{\partial E_{\text{pos}}}{\partial \epsilon_\theta} = \frac{1}{2}(13.375)(5)^2 = 167.19\text{ m/rad}$$
   *Con un desvío angular de $\epsilon_\theta = 0.1\text{ rad}$ ($5.73^\circ$), el desvío lateral acumulado es:*
   $$\Delta s_\theta = 167.19 \cdot 0.1 = 16.72\text{ m}$$

---

## 💻 3. Software de Simulación (`SimuladorINS.java`)

El proyecto incluye una aplicación desarrollada en **Java con interfaz gráfica Swing**, diseñada sin dependencias externas para garantizar compatibilidad nativa inmediata.

### Características del Sistema:
* **Panel de Control Paramétrico:** Permite ajustar interactivamente el tiempo total $t$, el sesgo del acelerómetro $da$ y el desvío del giroscopio $d\theta$.
* **Motor Numérico y Analítico:** Calcula instantáneamente orientación, velocidad, posición, velocidad media y derivadas parciales.
* **Módulo de Renderizado 2D Independiente:**
  * Curva continua de $v(t)$ con la línea horizontal del Teorema del Valor Promedio.
  * Curva teórica de $s(t)$ contrastada contra la curva con deriva inercial acumulada (área sombreada).
  * Cuadrícula con marcas de graduación (*ticks*) numéricas exteriores y recuadros de leyenda flotantes para evitar sobreposición visual.

---

## 🚀 4. Guía de Compilación y Ejecución

### Prerrequisitos
* Tener instalado el **Java Development Kit (JDK 8 o superior)**.

### Instrucciones por Consola / Terminal
1. Clonar o descargar el archivo `SimuladorINS.java` en una carpeta local.
2. Abrir la terminal en dicho directorio y compilar:
   ```bash
   javac SimuladorINS.java
   ```
3. Ejecutar la aplicación:
   ```bash
   java SimuladorINS
   ```

### Ejecución en Entornos de Desarrollo (IDEs)
* **IntelliJ IDEA / NetBeans / Eclipse:**
  1. Crear un proyecto Java estándar.
  2. Colocar `SimuladorINS.java` dentro del paquete por defecto o carpeta de fuentes `src`.
  3. Ejecutar la clase con `Run`.

---

## 📊 5. Tabla de Validación Cruzada (Cálculo Analítico vs. Software)

| Variable Cinemática | Valor Analítico (A Mano) | Valor Arrojado por el Software | Coincidencia |
| :--- | :---: | :---: | :---: |
| Orientación $\theta(5)$ | $0.25\text{ rad}\ (14.32^\circ)$ | $0.2500\text{ rad}\ (14.32^\circ)$ | Exacta ($100\%$) |
| Velocidad $v(5)$ | $10.49\text{ m/s}$ | $10.490\text{ m/s}$ | Exacta ($100\%$) |
| Posición $s(5)$ | $66.88\text{ m}$ | $66.875\text{ m}$ | Exacta ($100\%$) |
| Velocidad Media $V_{\text{prom}}$ | $13.38\text{ m/s}$ | $13.375\text{ m/s}$ | Exacta ($100\%$) |
| Sensibilidad Acel. $\partial E / \partial \epsilon_a$ | $12.50\text{ s}^2$ | $12.50\text{ s}^2$ | Exacta ($100\%$) |
| Sensibilidad Giro. $\partial E / \partial \epsilon_\theta$ | $167.19\text{ m/rad}$ | $167.19\text{ m/rad}$ | Exacta ($100\%$) |

---

## 📑 6. Conclusiones Técnicas del Proyecto

1. **Validación del Cálculo Integral en Sistemas Autónomos:** Se demostró cómo la doble integración temporal en tiempo real permite suplir eficazmente la pérdida de enlace satelital GPS, asegurando que el vehículo reconstruya su posición ($66.88\text{ m}$) y reduzca su velocidad de forma controlada a $10.49\text{ m/s}$ en el primer tercio del túnel.
2. **Importancia del Teorema del Valor Promedio:** El valor obtenido de $13.38\text{ m/s}$ sintetiza el régimen dinámico continuo del móvil durante los primeros 5 segundos, proporcionando una métrica global para los controladores PID de frenado regenerativo.
3. **Peligro Crítico del Drift y Rol de las Derivadas Parciales:** El análisis multivariable reveló que un error de orientación aparentemente insignificante ($0.1\text{ rad}$) provoca un desvío lateral de $16.72\text{ m}$, superando ampliamente el ancho de un carril vial e ilustrando por qué la corrección algorítmica (como filtros de Kalman) es indispensable en la ingeniería de software vehicular.