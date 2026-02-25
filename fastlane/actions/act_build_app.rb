module Fastlane
  module Actions
    class ActBuildAppAction < Action
      def self.run(params)
        env        = params[:env]
        build_type = params[:build_type]
        artifact   = params[:artifact]
        clean      = params[:clean]

        flavor = env.capitalize
        type   = build_type.capitalize

        task =
          if artifact == "apk"
            "assemble#{flavor}#{type}"
          else
            "bundle#{flavor}#{type}"
          end

        UI.message("Gradle task: #{task}")

        if clean
          other_action.gradle(task: "clean")
        end

        other_action.gradle(
          task: task,
          print_command: true
        )
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :env,
            description: "Environment / flavor",
            optional: true,
            default_value: "qa"
          ),
          FastlaneCore::ConfigItem.new(
            key: :build_type,
            description: "debug or release",
            optional: true,
            default_value: "release"
          ),
          FastlaneCore::ConfigItem.new(
            key: :artifact,
            description: "apk or aab",
            optional: true,
            default_value: "apk"
          ),
          FastlaneCore::ConfigItem.new(
            key: :clean,
            description: "Clean before build",
            optional: true,
            default_value: true,
            type: Boolean
          )
        ]
      end

      def self.description
        "Build app with sensible defaults"
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
