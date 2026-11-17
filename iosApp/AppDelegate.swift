import MiniTimerShared
import UIKit

@main
final class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        configurationForConnecting session: UISceneSession,
        options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        let configuration = UISceneConfiguration(name: nil, sessionRole: session.role)
        configuration.delegateClass = MiniTimerScene.self
        return configuration
    }
}

final class MiniTimerScene: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options: UIScene.ConnectionOptions) {
        guard let windowScene = scene as? UIWindowScene else { return }
        let storage = NotebookFile()
        let initial = storage.read()
        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = MiniTimerHostKt.miniTimerViewController(
            initialJson: initial.text,
            readFailed: initial.failed,
            writeJson: storage.write,
            copyText: { UIPasteboard.general.string = $0 }
        )
        self.window = window
        window.makeKeyAndVisible()
    }
}

private struct NotebookFile {
    private func fileURL() throws -> URL {
        try FileManager.default.url(
            for: .applicationSupportDirectory,
            in: .userDomainMask,
            appropriateFor: nil,
            create: true
        ).appendingPathComponent("notebook.json")
    }

    func read() -> (text: String?, failed: Bool) {
        do {
            return (try String(contentsOf: fileURL(), encoding: .utf8), false)
        } catch let error as CocoaError where error.code == .fileReadNoSuchFile {
            return (nil, false)
        } catch {
            return (nil, true)
        }
    }

    func write(_ text: String) -> String? {
        do {
            try Data(text.utf8).write(to: fileURL(), options: .atomic)
            return nil
        } catch {
            return error.localizedDescription
        }
    }
}
