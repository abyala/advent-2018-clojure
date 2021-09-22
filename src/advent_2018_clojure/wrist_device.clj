(ns advent-2018-clojure.wrist-device)

(defn register-op [op reg a b] (op (reg a) (reg b)))
(defn immediate-op [op reg a b] (op (reg a) b))
(def addr (partial register-op  +))
(def addi (partial immediate-op +))
(def mulr (partial register-op  *))
(def muli (partial immediate-op *))
(def banr (partial register-op  bit-and))
(def bani (partial immediate-op bit-and))
(def borr (partial register-op  bit-or))
(def bori (partial immediate-op bit-or))
(defn setr [reg a _] (reg a))
(defn seti [_ a _] a)
(defn compare-op [op a b] (if (op a b) 1 0))
(defn compare-op-imm-reg [op reg a b] (compare-op op a (reg b)))
(defn compare-op-reg-imm [op reg a b] (compare-op op (reg a) b))
(defn compare-op-reg-reg [op reg a b] (compare-op op (reg a) (reg b)))
(def gtir (partial compare-op-imm-reg >))
(def gtri (partial compare-op-reg-imm >))
(def gtrr (partial compare-op-reg-reg >))
(def eqir (partial compare-op-imm-reg =))
(def eqri (partial compare-op-reg-imm =))
(def eqrr (partial compare-op-reg-reg =))

(def operations (into {} (map #(vector (keyword %) (eval %))
                              ['addr 'addi 'mulr 'muli 'banr 'bani 'borr 'bori
                               'setr 'seti 'gtir 'gtri 'gtrr 'eqir 'eqri 'eqrr])))
(def operation-ids (keys operations))
(defn operation-named [name] (operations name))

(defn create-device [registers instructions & {:keys [operations ip-register]
                                               :or {operations operations ip-register nil}}]
  {:registers registers
   :instructions instructions
   :operations operations
   :ip-register ip-register
   :ip 0
   :halted? false})

(defn registers [device] (:registers device))
(defn ip [device] (:ip device))
(defn ip-register [device] (:ip-register device))
(defn halted? [device] (:halted? device))
(defn get-op [device op] (get-in device [:operations op]))

(defn- load-instruction-pointer [device]
  (if-let [reg (ip-register device)]
    (assoc-in device [:registers reg] (ip device))
    device))

(defn- store-instruction-pointer [device]
  (if-let [reg (ip-register device)]
    (assoc device :ip (get-in device [:registers reg]))
    device))

(defn- inc-instruction-pointer [device]
  (let [new-ip (inc (ip device))]
    (assoc device :ip new-ip
                  :halted? (nil? (get-in device [:instructions new-ip])))))

(defn- run-operation-at-instruction-pointer [device]
  (let [[op a b c] (get-in device [:instructions (ip device)])
        regs (registers device)]
    (assoc-in device [:registers c] ((get-op device op) regs a b))))

(defn run-operation [device]
  (when-not (halted? device)
    (-> device
        load-instruction-pointer
        run-operation-at-instruction-pointer
        store-instruction-pointer
        inc-instruction-pointer)))

(defn run-all-steps [device]
  (->> (iterate run-operation device)
       (take-while some?)))

(defn run-to-completion [device]
  (-> device run-all-steps last))