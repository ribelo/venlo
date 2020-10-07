(ns ribelo.venlo
  (:require
   [cheshire.core :as json]
   [cuerdas.core :as str]
   [hiccup.core :refer [html]]
   [ribelo.venlo.echarts :as echarts]))

(defn ->chart
  ([{:keys [series] :as opts}]
   (assoc opts :series (mapv echarts/->series series)))
  ([{:keys [series] :as opts} coll]
   (assoc opts :series (mapv #(echarts/->series % coll) series))))

(defn plot [{:keys [width height theme]
             :or   {width 900 height 400 theme "default"}
             :as   opts}]
  (let [id   (str (java.util.UUID/randomUUID))
        code (format "var chart = echarts.init(document.getElementById('%s'), '%s');
                      chart.setOption(%s)"
                     id theme (json/generate-string (-> opts (dissoc :width) (dissoc :height))
                                                    {:key-fn str/camel}))]
    (html
      [:div [:div {:id id :style (format (str "width:%spx;"
                                              "height:%spx;"
                                              "page-break-inside:avoid")
                                         width height)}]
       [:script code]])))
